package com.nexora.component;

import com.nexora.entity.po.AiGenerationRecord;
import com.nexora.entity.po.ResourceDirectory;
import com.nexora.entity.po.ResourceInfo;
import com.nexora.service.AiGenerationRecordService;
import com.nexora.service.ResourceInfoService;
import com.nexora.service.StudentKnowledgeBaseService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

/**
 * 动画讲解产物落库：AI 生成记录 + 个人知识库 ANIMATION 资源（附件目录）。
 * AI 助教对话生成与动画讲解页独立生成共用，保证两条链路产物结构一致。
 */
@Slf4j
@Component
public class AnimationSaveComponent {

    @Resource
    private AiGenerationRecordService aiGenerationRecordService;

    @Resource
    private ResourceInfoService resourceInfoService;

    @Resource
    private StudentKnowledgeBaseService studentKnowledgeBaseService;

    /**
     * 落 AI 生成记录与个人知识库资源，返回动画资源记录
     */
    public ResourceInfo save(String userId, String stage, String title, String scriptJson) {
        Date now = new Date();

        AiGenerationRecord record = new AiGenerationRecord();
        record.setRecordId(UUID.randomUUID().toString().replace("-", ""));
        record.setUserId(userId);
        record.setStage(stage);
        record.setType("ANIMATION");
        record.setTitle(title);
        record.setContent(scriptJson);
        record.setSource(0);
        record.setStatus(1);
        record.setSaved(0);
        record.setAuditStatus(0);
        record.setCreateTime(now);
        record.setUpdateTime(now);
        aiGenerationRecordService.add(record);

        ResourceDirectory dir = studentKnowledgeBaseService.getSystemDirectory(
                userId, StudentKnowledgeBaseService.DIR_TYPE_ATTACHMENTS);
        ResourceInfo resource = new ResourceInfo();
        resource.setResourceId(UUID.randomUUID().toString().replace("-", ""));
        resource.setResourceName("动画讲解-" + title);
        resource.setResourceType("ANIMATION");
        resource.setExtJson(scriptJson);
        resource.setDirectoryId(dir == null ? null : dir.getDirId());
        resource.setStage(stage);
        resource.setOwnerId(userId);
        resource.setSource(1);
        resource.setStatus(1);
        resource.setCreateTime(now);
        resource.setUpdateTime(now);
        resourceInfoService.add(resource);
        log.info("动画产物已落库 resourceId={} userId={} title={}", resource.getResourceId(), userId, title);
        return resource;
    }
}