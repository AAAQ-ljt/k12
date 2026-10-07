package com.nexora.service.impl;

import com.nexora.component.AiStructureComponent;
import com.nexora.component.PreferencePageComponent;
import com.nexora.component.PointAwardComponent;
import com.nexora.component.ResourceKnowledgeParser;
import com.nexora.component.WikiKnowledgeComponent;
import com.nexora.dto.StudentWikiProfileDTO;
import com.nexora.entity.po.AgentMessage;
import com.nexora.entity.po.KnowledgeDoc;
import com.nexora.entity.po.ResourceInfo;
import com.nexora.entity.po.UserWikiProfile;
import com.nexora.entity.query.CourseChapterLessonQuery;
import com.nexora.entity.query.CourseChapterLessonResourceQuery;
import com.nexora.entity.query.CourseChapterQuery;
import com.nexora.entity.query.KnowledgeDocQuery;
import com.nexora.entity.po.CourseChapter;
import com.nexora.entity.po.CourseChapterLesson;
import com.nexora.entity.po.CourseChapterLessonResource;
import com.nexora.exception.BusinessException;
import com.nexora.service.AgentMessageService;
import com.nexora.service.CourseChapterLessonResourceService;
import com.nexora.service.CourseChapterLessonService;
import com.nexora.service.CourseChapterService;
import com.nexora.service.KnowledgeDocService;
import com.nexora.service.ResourceInfoService;
import com.nexora.service.StudentWikiService;
import com.nexora.service.UserWikiProfileService;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 学生个人知识页（wiki 层）业务实现：两段式「生成草稿 → 用户确认 → 向量化」
 */
@Service
public class StudentWikiServiceImpl implements StudentWikiService {

    private static final Logger log = LoggerFactory.getLogger(StudentWikiServiceImpl.class);

    /** 偏好页保护与折叠渲染（计划 C3） */
    @Resource
    private PreferencePageComponent preferencePageComponent;

    @Resource
    private KnowledgeDocService knowledgeDocService;

    /** 积分发放入口（知识页确认入库）：积分异常只记日志、不影响入库 */
    @Resource
    private PointAwardComponent pointAwardComponent;

    @Resource
    private ResourceInfoService resourceInfoService;

    @Resource
    private ResourceKnowledgeParser resourceKnowledgeParser;

    @Resource
    private AiStructureComponent aiStructureComponent;

    @Resource
    private WikiKnowledgeComponent wikiKnowledgeComponent;

    @Resource
    private UserWikiProfileService userWikiProfileService;

    @Resource
    private AgentMessageService agentMessageService;

    @Resource
    private CourseChapterService courseChapterService;

    @Resource
    private CourseChapterLessonService courseChapterLessonService;

    @Resource
    private CourseChapterLessonResourceService courseChapterLessonResourceService;

    @Override
    public KnowledgeDoc generateDraft(String userId, String resourceId) {
        ResourceInfo resource = assertOwnedResource(userId, resourceId);
        if (resource.getStatus() == null || resource.getStatus() != 1) {
            throw new BusinessException("资源暂不可用，请稍后再试");
        }
        if (!"DOCUMENT".equalsIgnoreCase(resource.getResourceType())) {
            throw new BusinessException("仅文档资源可生成知识页");
        }
        ResourceKnowledgeParser.ParseResult parsed = resourceKnowledgeParser.parse(resource);
        String text = parsed == null ? null : parsed.getText();
        if (StringTools.isEmpty(text)) {
            log.warn("知识页生成失败：文档未提取到文本 resourceId={}", resourceId);
            throw new BusinessException("未能从文档提取到文本，请确认文档内容可读取");
        }
        String structured = aiStructureComponent.generateStructure(resource.getStage(), resource.getResourceName(), text);
        if (StringTools.isEmpty(structured)) {
            throw new BusinessException("AI 整理结果为空，请稍后重试");
        }
        return wikiKnowledgeComponent.saveDraftBySource(userId, resource.getStage(), resource.getResourceName(),
                resourceId, structured, null);
    }

    /**
     * 偏好页保护（计划 C3）：编辑走"折叠渲染"——只取学生写的自由段，规则段由系统重新生成，
     * 避免学生把规则段改坏；其它知识页走原逻辑。
     */
    @Override
    public KnowledgeDoc updateDraft(String userId, String docId, String content) {
        if (preferencePageComponent.saveFromEditor(userId, docId, content)) {
            return getDraft(userId, docId);
        }
        // 覆盖内容并回草稿态（旧向量由公共组件就地清理或投递清理任务）
        return wikiKnowledgeComponent.overwriteAsDraft(userId, docId, null, content);
    }

    @Override
    public KnowledgeDoc confirm(String userId, String docId) {
        // 偏好页不做向量化（计划 C3）：它是给 AI 的个人资料，入库会进入 RAG 检索、反而干扰
        if (preferencePageComponent.isPreferencePage(docId, userId)) {
            throw new BusinessException("《我的学习偏好》不参与资料检索（系统页不做向量化），无需入库");
        }
        KnowledgeDoc doc = wikiKnowledgeComponent.requireOwnedDoc(userId, docId);
        // 图片/视频轻量页：确认时刷新为最新标题+简介（用户手动编辑过的 content 不覆盖）
        if (isLightweightType(doc.getDataType())) {
            refreshLightweightContent(doc);
        }
        // 状态校验（内容为空 / 向量化中 / 已入库）与入队由公共组件统一处理
        KnowledgeDoc ingested = wikiKnowledgeComponent.markIngest(userId, docId);
        // 确认入库 → 积分（bizId=知识页ID，同一页只奖一次；受每日上限约束）—— 二期规划 A-2 第 11 条。
        // 积分异常只记日志：入库本身已成功，绝不因为积分把入库回滚。
        try {
            pointAwardComponent.awardWikiConfirm(userId, doc.getStage(), docId);
        } catch (Exception e) {
            log.warn("知识页入库积分发放失败（不影响入库）userId={} docId={}", userId, docId, e);
        }
        return ingested;
    }

    @Override
    public KnowledgeDoc getDraft(String userId, String docId) {
        return wikiKnowledgeComponent.requireOwnedDoc(userId, docId);
    }

    @Override
    public List<KnowledgeDoc> listDrafts(String userId, String resourceId) {
        return listDrafts(userId, resourceId, null);
    }

    @Override
    public List<KnowledgeDoc> listDrafts(String userId, String resourceId, String folderId) {
        KnowledgeDocQuery query = new KnowledgeDocQuery();
        query.setOwnerId(userId);
        if (!StringTools.isEmpty(resourceId)) {
            query.setSourceResourceId(resourceId);
        }
        if (!StringTools.isEmpty(folderId)) {
            // "root"=仅根目录；具体文件夹ID=该文件夹内；不传=全部
            if ("root".equals(folderId)) {
                query.setFolderIdNull(Boolean.TRUE);
            } else {
                query.setFolderId(folderId);
            }
        }
        query.setOrderBy("update_time desc");
        return knowledgeDocService.findListByParam(query);
    }

    @Override
    public String createFolder(String userId, String name, String parentFolderId) {
        return wikiKnowledgeComponent.createWikiFolder(userId, name, parentFolderId);
    }

    @Override
    public void moveDoc(String userId, String docId, String folderId) {
        if (preferencePageComponent.isPreferencePage(docId, userId)) {
            throw new BusinessException("《我的学习偏好》固定在知识页根目录，不能移动");
        }
        wikiKnowledgeComponent.moveDocToFolder(userId, docId, folderId);
    }

    @Override
    public void deleteFolder(String userId, String folderId) {
        wikiKnowledgeComponent.deleteWikiFolder(userId, folderId);
    }

    @Override
    public void deleteDraft(String userId, String docId) {
        if (preferencePageComponent.isPreferencePage(docId, userId)) {
            throw new BusinessException("《我的学习偏好》是系统页，不能删除；想恢复初始内容请用「重置」");
        }
        wikiKnowledgeComponent.deleteOwned(userId, docId);
    }

    @Override
    public void cleanupByResource(String userId, String resourceId) {
        if (StringTools.isEmpty(resourceId)) {
            // 防御（2026-10-04）：资源ID为空时不做级联清理——若查询条件失效会误清该学生全部知识页
            log.warn("资源删除级联清理跳过：resourceId 为空 userId={}", userId);
            return;
        }
        KnowledgeDocQuery query = new KnowledgeDocQuery();
        query.setOwnerId(userId);
        query.setSourceResourceId(resourceId);
        List<KnowledgeDoc> docs = knowledgeDocService.findListByParam(query);
        for (KnowledgeDoc doc : docs) {
            try {
                wikiKnowledgeComponent.clearVector(doc);
                knowledgeDocService.deleteKnowledgeDocByDocId(doc.getDocId());
            } catch (Exception e) {
                log.warn("资源删除时清理知识页失败 docId={} resourceId={}", doc.getDocId(), resourceId, e);
            }
        }
    }

    @Override
    public UserWikiProfile getProfile(String userId) {
        return userWikiProfileService.getUserWikiProfileByUserId(userId);
    }

    @Override
    public UserWikiProfile saveProfile(String userId, StudentWikiProfileDTO dto) {
        Date now = new Date();
        UserWikiProfile profile = userWikiProfileService.getUserWikiProfileByUserId(userId);
        if (profile == null) {
            profile = new UserWikiProfile();
            profile.setUserId(userId);
            profile.setLearningGoal(dto.getLearningGoal());
            profile.setKeyQuestions(dto.getKeyQuestions());
            profile.setInterestSubjects(dto.getInterestSubjects());
            profile.setAliasTerms(dto.getAliasTerms());
            profile.setCreateTime(now);
            profile.setUpdateTime(now);
            userWikiProfileService.add(profile);
        } else {
            UserWikiProfile update = new UserWikiProfile();
            update.setLearningGoal(dto.getLearningGoal());
            update.setKeyQuestions(dto.getKeyQuestions());
            update.setInterestSubjects(dto.getInterestSubjects());
            update.setAliasTerms(dto.getAliasTerms());
            update.setUpdateTime(now);
            userWikiProfileService.updateUserWikiProfileByUserId(update, userId);
            profile.setLearningGoal(dto.getLearningGoal());
            profile.setKeyQuestions(dto.getKeyQuestions());
            profile.setInterestSubjects(dto.getInterestSubjects());
            profile.setAliasTerms(dto.getAliasTerms());
            profile.setUpdateTime(now);
        }
        return profile;
    }

    @Override
    public KnowledgeDoc syncFromMessage(String userId, String messageId) {
        AgentMessage message = agentMessageService.getAgentMessageByMessageId(messageId);
        if (message == null || !userId.equals(message.getUserId())) {
            throw new BusinessException("消息不存在或无权操作");
        }
        if (message.getStatus() == null || message.getStatus() != 1
                || StringTools.isEmpty(message.getAssistantMessage())) {
            throw new BusinessException("该消息尚未完成回答，无法同步");
        }
        if (StringTools.isEmpty(message.getUserMessage())) {
            throw new BusinessException("问题内容为空");
        }
        String title = message.getUserMessage().trim();
        if (title.length() > 30) {
            title = title.substring(0, 30);
        }
        String content = "## 我的问题\n\n" + message.getUserMessage().trim()
                + "\n\n## AI 讲解\n\n" + message.getAssistantMessage().trim();
        // 以 source_url 承载消息标识做去重（同一消息重复同步则覆盖草稿）
        return wikiKnowledgeComponent.saveDraftBySource(userId, message.getStage(), title, null, content,
                "agent-message:" + messageId);
    }

    @Override
    public KnowledgeDoc syncFromCourse(String userId, String stage, String courseId, String courseTitle) {
        StringBuilder material = new StringBuilder();
        material.append("课程：").append(courseTitle == null ? "" : courseTitle).append("\n\n");
        int resourceCount = 0;
        CourseChapterQuery chapterQuery = new CourseChapterQuery();
        chapterQuery.setCourseId(courseId);
        chapterQuery.setStatus(0);
        chapterQuery.setOrderBy("sort asc, create_time asc");
        List<CourseChapter> chapters = courseChapterService.findListByParam(chapterQuery);
        for (CourseChapter chapter : chapters) {
            CourseChapterLessonQuery lessonQuery = new CourseChapterLessonQuery();
            lessonQuery.setChapterId(chapter.getChapterId());
            lessonQuery.setCourseId(courseId);
            lessonQuery.setStatus(0);
            lessonQuery.setOrderBy("sort asc, create_time asc");
            List<CourseChapterLesson> lessons = courseChapterLessonService.findListByParam(lessonQuery);
            for (CourseChapterLesson lesson : lessons) {
                CourseChapterLessonResourceQuery resourceQuery = new CourseChapterLessonResourceQuery();
                resourceQuery.setLessonId(lesson.getLessonId());
                resourceQuery.setOrderBy("sort asc, id asc");
                List<CourseChapterLessonResource> binds = courseChapterLessonResourceService.findListByParam(resourceQuery);
                for (CourseChapterLessonResource bind : binds) {
                    if (resourceCount >= 30) {
                        break;
                    }
                    ResourceInfo resource = resourceInfoService.getResourceInfoByResourceId(bind.getResourceId());
                    if (resource == null || resource.getStatus() == null || resource.getStatus() != 1) {
                        continue;
                    }
                    material.append("章节《").append(chapter.getChapterName()).append("》课时《")
                            .append(lesson.getLessonName()).append("》资料《")
                            .append(resource.getResourceName()).append("》");
                    if (!StringTools.isEmpty(resource.getDescription())) {
                        material.append("（简介：").append(resource.getDescription().trim()).append("）");
                    }
                    material.append("\n");
                    resourceCount++;
                }
            }
        }
        if (resourceCount == 0) {
            throw new BusinessException("课程暂无可用的学习资料");
        }
        String structured = aiStructureComponent.generateStructure(stage, courseTitle, material.toString());
        if (StringTools.isEmpty(structured)) {
            throw new BusinessException("AI 整理结果为空，请稍后重试");
        }
        // 以 source_url 承载课程标识做去重（同一课程重复同步则覆盖草稿）
        return wikiKnowledgeComponent.saveDraftBySource(userId, stage, courseTitle, null, structured,
                "course:" + courseId);
    }

    private boolean isLightweightType(String dataType) {
        return "IMAGE".equalsIgnoreCase(dataType) || "VIDEO".equalsIgnoreCase(dataType);
    }

    /**
     * 图片/视频轻量页确认入库前刷新为最新标题+简介（自动模板内容以「标题：」开头；用户手动编辑过的不覆盖）
     */
    private void refreshLightweightContent(KnowledgeDoc doc) {
        try {
            ResourceInfo resource = resourceInfoService.getResourceInfoByResourceId(doc.getSourceResourceId());
            if (resource == null) {
                return;
            }
            String content = StudentResourceUploadServiceImpl.refreshLightweightContent(
                    doc.getContent(), resource.getResourceName(), resource.getDescription());
            if (content == null || content.equals(doc.getContent())) {
                return;
            }
            KnowledgeDoc update = new KnowledgeDoc();
            update.setContent(content);
            update.setTitle(resource.getResourceName());
            update.setUpdateTime(new Date());
            knowledgeDocService.updateKnowledgeDocByDocId(update, doc.getDocId());
        } catch (Exception e) {
            log.warn("图片/视频轻量页内容刷新失败 docId={}", doc.getDocId(), e);
        }
    }

    private ResourceInfo assertOwnedResource(String userId, String resourceId) {
        ResourceInfo resource = resourceInfoService.getResourceInfoByResourceId(resourceId);
        if (resource == null || !userId.equals(resource.getOwnerId())) {
            throw new BusinessException("资源不存在或无权操作");
        }
        return resource;
    }
}