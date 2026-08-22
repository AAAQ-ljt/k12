package com.nexora.service.impl;

import com.nexora.component.LearningPathGenerateComponent;
import com.nexora.entity.po.AiGenerationRecord;
import com.nexora.entity.po.KnowledgeDoc;
import com.nexora.entity.po.UserWikiProfile;
import com.nexora.entity.query.AiGenerationRecordQuery;
import com.nexora.entity.query.KnowledgeDocQuery;
import com.nexora.exception.BusinessException;
import com.nexora.service.AiGenerationRecordService;
import com.nexora.service.KnowledgeDocService;
import com.nexora.service.LearningPathService;
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
 * 学生个性化学习路径业务实现：学习档案 + 已学知识 → AI 生成学习计划 → ai_generation_record
 */
@Service
public class LearningPathServiceImpl implements LearningPathService {

    private static final Logger log = LoggerFactory.getLogger(LearningPathServiceImpl.class);

    private static final String RECORD_TYPE = "LEARNING_PATH";

    @Resource
    private AiGenerationRecordService aiGenerationRecordService;

    @Resource
    private UserWikiProfileService userWikiProfileService;

    @Resource
    private KnowledgeDocService knowledgeDocService;

    @Resource
    private LearningPathGenerateComponent learningPathGenerateComponent;

    @Override
    public AiGenerationRecord generate(String userId, String stage) {
        String profileText = buildProfileText(userId);
        List<String> learnedTitles = listLearnedTitles(userId);
        LearningPathGenerateComponent.LearningPathPlan plan =
                learningPathGenerateComponent.generate(stage, profileText, learnedTitles);
        Date now = new Date();
        AiGenerationRecord record = new AiGenerationRecord();
        record.setRecordId(UUID.randomUUID().toString().replace("-", ""));
        record.setUserId(userId);
        record.setStage(stage);
        record.setType(RECORD_TYPE);
        record.setTitle(plan.title());
        record.setContent(plan.toJson());
        record.setSource(0);
        record.setStatus(1);
        record.setSaved(0);
        record.setAuditStatus(0);
        record.setCreateTime(now);
        record.setUpdateTime(now);
        aiGenerationRecordService.add(record);
        log.info("学习路径生成完成 userId={} title={} steps={}", userId, plan.title(), plan.steps().size());
        return record;
    }

    @Override
    public List<AiGenerationRecord> myList(String userId) {
        AiGenerationRecordQuery query = new AiGenerationRecordQuery();
        query.setUserId(userId);
        query.setType(RECORD_TYPE);
        query.setOrderBy("create_time desc");
        return aiGenerationRecordService.findListByParam(query);
    }

    @Override
    public void delete(String userId, String recordId) {
        AiGenerationRecord record = aiGenerationRecordService.getAiGenerationRecordByRecordId(recordId);
        if (record == null || !userId.equals(record.getUserId())) {
            throw new BusinessException("学习路径不存在或无权操作");
        }
        aiGenerationRecordService.deleteAiGenerationRecordByRecordId(recordId);
    }

    private String buildProfileText(String userId) {
        UserWikiProfile profile = userWikiProfileService.getUserWikiProfileByUserId(userId);
        if (profile == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        if (!StringTools.isEmpty(profile.getLearningGoal())) {
            builder.append("学习目标：").append(profile.getLearningGoal()).append("\n");
        }
        if (!StringTools.isEmpty(profile.getInterestSubjects())) {
            builder.append("感兴趣学科/主题：").append(profile.getInterestSubjects()).append("\n");
        }
        if (!StringTools.isEmpty(profile.getKeyQuestions())) {
            builder.append("关键问题：").append(profile.getKeyQuestions()).append("\n");
        }
        if (!StringTools.isEmpty(profile.getAliasTerms())) {
            builder.append("我的术语叫法：").append(profile.getAliasTerms()).append("\n");
        }
        return builder.toString().trim();
    }

    private List<String> listLearnedTitles(String userId) {
        // 已确认入向量库的知识页视为已学
        KnowledgeDocQuery query = new KnowledgeDocQuery();
        query.setOwnerId(userId);
        query.setVectorStatus(2);
        query.setOrderBy("update_time desc");
        List<KnowledgeDoc> docs = knowledgeDocService.findListByParam(query);
        return docs.stream().map(KnowledgeDoc::getTitle).filter(t -> !StringTools.isEmpty(t)).toList();
    }
}