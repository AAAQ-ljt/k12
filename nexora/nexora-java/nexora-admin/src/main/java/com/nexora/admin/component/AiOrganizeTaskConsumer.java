package com.nexora.admin.component;

import com.nexora.admin.biz.AiOrganizeTaskBiz;
import com.nexora.admin.biz.KnowledgeBaseBiz;
import com.nexora.admin.dto.AiOrganizeTaskVO;
import com.nexora.admin.vo.KnowledgeAIDocVO;
import com.nexora.component.RedisComponent;
import com.nexora.constants.Constants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * AI 文档整理异步任务消费者：
 * 状态机 PENDING → ORGANIZING → COMPLETED / FAILED；
 * 执行 ResourceKnowledgeParser 文本提取 + AiStructureComponent 结构化（复用 KnowledgeBaseBiz.aiOrganizeCore，解析逻辑零改动），
 * 整理结果写回任务体，前端轮询完成后进入「左原文对照 + 右可编辑」工作面板；切页按 taskId 恢复，状态不丢。
 */
@Slf4j
@Component
public class AiOrganizeTaskConsumer {

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private AiOrganizeTaskBiz aiOrganizeTaskBiz;

    @Resource
    private KnowledgeBaseBiz knowledgeBaseBiz;

    @Scheduled(fixedDelay = 1000)
    public void consume() {
        Object taskIdObj = redisComponent.rightPop(Constants.REDIS_KEY_AI_ORGANIZE_TASK_QUEUE);
        if (taskIdObj == null) {
            return;
        }
        String taskId = taskIdObj.toString();
        AiOrganizeTaskVO task;
        try {
            task = aiOrganizeTaskBiz.loadInternal(taskId);
        } catch (Exception e) {
            log.warn("AI 文档整理任务读取失败 taskId={}", taskId, e);
            return;
        }
        if (task == null) {
            log.warn("AI 文档整理任务不存在或已过期 taskId={}", taskId);
            return;
        }
        try {
            execute(task);
        } catch (Exception e) {
            log.error("AI 文档整理任务执行异常 taskId={} resourceId={}", taskId, task.getResourceId(), e);
            task.setStatus("FAILED");
            task.setMessage("AI 整理失败：" + e.getMessage());
            aiOrganizeTaskBiz.update(task);
        } finally {
            aiOrganizeTaskBiz.releaseRunning(task.getResourceId(), taskId);
        }
    }

    private void execute(AiOrganizeTaskVO task) throws Exception {
        task.setStatus("ORGANIZING");
        task.setMessage("AI 正在提取文本并整理为结构化 Markdown，大文档可能耗时较长...");
        aiOrganizeTaskBiz.update(task);

        KnowledgeAIDocVO organized = knowledgeBaseBiz.aiOrganizeCore(task.getResourceId());

        task.setStatus("COMPLETED");
        task.setMessage("AI 整理完成");
        task.setResourceName(organized.getResourceName());
        task.setStage(organized.getStage());
        task.setOrganizedMd(organized.getOrganizedMd());
        task.setOriginalText(organized.getOriginalText());
        aiOrganizeTaskBiz.update(task);
        log.info("AI 文档整理任务完成 taskId={} resourceId={} 整理稿长度={}",
                task.getTaskId(), task.getResourceId(),
                organized.getOrganizedMd() == null ? 0 : organized.getOrganizedMd().length());
    }
}