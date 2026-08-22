package com.nexora.service.impl;

import com.nexora.component.AiStructureComponent;
import com.nexora.component.KnowledgeVectorComponent;
import com.nexora.component.RedisComponent;
import com.nexora.component.ResourceKnowledgeParser;
import com.nexora.constants.Constants;
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
import java.util.UUID;

/**
 * 学生个人知识页（wiki 层）业务实现：两段式「生成草稿 → 用户确认 → 向量化」
 */
@Service
public class StudentWikiServiceImpl implements StudentWikiService {

    private static final Logger log = LoggerFactory.getLogger(StudentWikiServiceImpl.class);

    /** 学生知识页未关联知识点，统一 0 占位（与既有个人库约定一致） */
    private static final String PLACEHOLDER_KNOWLEDGE_POINT_ID = "0";

    @Resource
    private KnowledgeDocService knowledgeDocService;

    @Resource
    private ResourceInfoService resourceInfoService;

    @Resource
    private ResourceKnowledgeParser resourceKnowledgeParser;

    @Resource
    private AiStructureComponent aiStructureComponent;

    @Resource
    private KnowledgeVectorComponent knowledgeVectorComponent;

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

    @Resource
    private RedisComponent redisComponent;

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
        return saveDraft(userId, resource.getStage(), resource.getResourceName(), resourceId, structured, null);
    }

    @Override
    public KnowledgeDoc updateDraft(String userId, String docId, String content) {
        if (StringTools.isEmpty(content)) {
            throw new BusinessException("知识页内容不能为空");
        }
        KnowledgeDoc doc = assertOwnedDoc(userId, docId);
        clearVectorIfConfirmed(doc);
        KnowledgeDoc update = new KnowledgeDoc();
        update.setContent(content);
        update.setVectorStatus(0);
        update.setVectorError(null);
        update.setChunkCount(0);
        update.setUpdateTime(new Date());
        knowledgeDocService.updateKnowledgeDocByDocId(update, docId);
        return knowledgeDocService.getKnowledgeDocByDocId(docId);
    }

    @Override
    public KnowledgeDoc confirm(String userId, String docId) {
        KnowledgeDoc doc = assertOwnedDoc(userId, docId);
        if (StringTools.isEmpty(doc.getContent())) {
            throw new BusinessException("知识页内容为空，无法确认");
        }
        if (doc.getVectorStatus() != null && doc.getVectorStatus() == 1) {
            throw new BusinessException("知识页正在向量化中，请稍候");
        }
        if (doc.getVectorStatus() != null && doc.getVectorStatus() == 2) {
            throw new BusinessException("知识页已确认入库");
        }
        // 图片/视频轻量页：确认时刷新为最新标题+简介（用户手动编辑过的 content 不覆盖）
        if (isLightweightType(doc.getDataType())) {
            refreshLightweightContent(doc);
        }
        KnowledgeDoc update = new KnowledgeDoc();
        update.setVectorStatus(1);
        update.setVectorError(null);
        update.setUpdateTime(new Date());
        knowledgeDocService.updateKnowledgeDocByDocId(update, docId);
        redisComponent.leftPush(Constants.REDIS_KEY_STUDENT_KNOWLEDGE_QUEUE, docId);
        return knowledgeDocService.getKnowledgeDocByDocId(docId);
    }

    @Override
    public KnowledgeDoc getDraft(String userId, String docId) {
        return assertOwnedDoc(userId, docId);
    }

    @Override
    public List<KnowledgeDoc> listDrafts(String userId, String resourceId) {
        KnowledgeDocQuery query = new KnowledgeDocQuery();
        query.setOwnerId(userId);
        if (!StringTools.isEmpty(resourceId)) {
            query.setSourceResourceId(resourceId);
        }
        query.setOrderBy("update_time desc");
        return knowledgeDocService.findListByParam(query);
    }

    @Override
    public void deleteDraft(String userId, String docId) {
        KnowledgeDoc doc = assertOwnedDoc(userId, docId);
        clearVectorIfConfirmed(doc);
        knowledgeDocService.deleteKnowledgeDocByDocId(docId);
    }

    @Override
    public void cleanupByResource(String userId, String resourceId) {
        KnowledgeDocQuery query = new KnowledgeDocQuery();
        query.setOwnerId(userId);
        query.setSourceResourceId(resourceId);
        List<KnowledgeDoc> docs = knowledgeDocService.findListByParam(query);
        for (KnowledgeDoc doc : docs) {
            try {
                clearVectorIfConfirmed(doc);
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
        return saveDraft(userId, message.getStage(), title, null, content, "agent-message:" + messageId);
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
        return saveDraft(userId, stage, courseTitle, null, structured, "course:" + courseId);
    }

    private KnowledgeDoc saveDraft(String userId, String stage, String title, String resourceId, String content, String sourceUrl) {
        KnowledgeDocQuery query = new KnowledgeDocQuery();
        query.setOwnerId(userId);
        if (!StringTools.isEmpty(resourceId)) {
            query.setSourceResourceId(resourceId);
        }
        if (!StringTools.isEmpty(sourceUrl)) {
            query.setSourceUrl(sourceUrl);
        }
        List<KnowledgeDoc> existing = knowledgeDocService.findListByParam(query);
        Date now = new Date();
        if (existing != null && !existing.isEmpty()) {
            KnowledgeDoc doc = existing.get(0);
            clearVectorIfConfirmed(doc);
            KnowledgeDoc update = new KnowledgeDoc();
            update.setTitle(title);
            update.setContent(content);
            update.setVectorStatus(0);
            update.setVectorError(null);
            update.setChunkCount(0);
            if (!StringTools.isEmpty(sourceUrl)) {
                update.setSourceUrl(sourceUrl);
            }
            update.setUpdateTime(now);
            knowledgeDocService.updateKnowledgeDocByDocId(update, doc.getDocId());
            return knowledgeDocService.getKnowledgeDocByDocId(doc.getDocId());
        }
        KnowledgeDoc doc = new KnowledgeDoc();
        doc.setDocId(UUID.randomUUID().toString().replace("-", ""));
        doc.setTitle(title);
        doc.setStage(stage);
        doc.setOwnerId(userId);
        doc.setKnowledgePointId(PLACEHOLDER_KNOWLEDGE_POINT_ID);
        doc.setDifficulty(1);
        doc.setDataType("KNOWLEDGE");
        doc.setContent(content);
        doc.setSourceType(0);
        doc.setSourceResourceId(resourceId);
        doc.setSourceUrl(sourceUrl);
        doc.setVectorStatus(0);
        doc.setVectorError(null);
        doc.setChunkCount(0);
        doc.setStatus(1);
        doc.setCreateTime(now);
        doc.setUpdateTime(now);
        knowledgeDocService.add(doc);
        return doc;
    }

    private void clearVectorIfConfirmed(KnowledgeDoc doc) {
        int chunkCount = doc.getChunkCount() == null ? 0 : doc.getChunkCount();
        if (chunkCount > 0) {
            try {
                knowledgeVectorComponent.deleteChunks(doc.getDocId(), chunkCount);
            } catch (Exception e) {
                log.warn("清理知识页向量失败 docId={}", doc.getDocId(), e);
            }
        }
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

    private KnowledgeDoc assertOwnedDoc(String userId, String docId) {
        KnowledgeDoc doc = knowledgeDocService.getKnowledgeDocByDocId(docId);
        if (doc == null || !userId.equals(doc.getOwnerId())) {
            throw new BusinessException("知识页不存在或无权操作");
        }
        return doc;
    }

    private ResourceInfo assertOwnedResource(String userId, String resourceId) {
        ResourceInfo resource = resourceInfoService.getResourceInfoByResourceId(resourceId);
        if (resource == null || !userId.equals(resource.getOwnerId())) {
            throw new BusinessException("资源不存在或无权操作");
        }
        return resource;
    }
}