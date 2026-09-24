package com.nexora.admin.component;

import com.nexora.admin.biz.KnowledgeBaseBiz;
import com.nexora.admin.biz.KnowledgeImportTaskBiz;
import com.nexora.admin.dto.KnowledgeImportTaskVO;
import com.nexora.component.RedisComponent;
import com.nexora.constants.Constants;
import com.nexora.entity.po.KnowledgeDoc;
import com.nexora.service.KnowledgeDocService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 知识库解析入库任务消费者：
 * 状态机 PENDING → EXTRACTING（资源文本提取）→ VECTORIZING（分块向量化）→ COMPLETED / FAILED；
 * 直录文档跳过 EXTRACTING。终态同时落 knowledge_doc.vectorStatus/chunkCount（列表与检索不受影响），
 * 解析与向量化逻辑复用 KnowledgeBaseBiz（不做任何解析算法改动）。
 */
@Slf4j
@Component
public class KnowledgeImportTaskConsumer {

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private KnowledgeImportTaskBiz knowledgeImportTaskBiz;

    @Resource
    private KnowledgeBaseBiz knowledgeBaseBiz;

    @Resource
    private KnowledgeDocService knowledgeDocService;

    @Scheduled(fixedDelay = 1000)
    public void consume() {
        Object taskIdObj = redisComponent.rightPop(Constants.REDIS_KEY_KNOWLEDGE_IMPORT_TASK_QUEUE);
        if (taskIdObj == null) {
            return;
        }
        String taskId = taskIdObj.toString();
        KnowledgeImportTaskVO task;
        try {
            task = knowledgeImportTaskBiz.loadInternal(taskId);
        } catch (Exception e) {
            log.warn("知识库解析入库任务读取失败 taskId={}", taskId, e);
            return;
        }
        if (task == null) {
            log.warn("知识库解析入库任务不存在或已过期 taskId={}", taskId);
            return;
        }
        try {
            execute(task);
        } catch (Exception e) {
            log.error("知识库解析入库任务执行异常 taskId={} docId={}", taskId, task.getDocId(), e);
            task.setStatus("FAILED");
            task.setMessage("解析入库失败：" + e.getMessage());
            task.setProgress(100);
            knowledgeImportTaskBiz.update(task);
        } finally {
            knowledgeImportTaskBiz.releaseRunning(task.getDocId(), taskId);
        }
    }

    private void execute(KnowledgeImportTaskVO task) throws Exception {
        boolean needsExtract = task.getSourceType() != null && task.getSourceType() == 1;
        if (needsExtract) {
            task.setStatus("EXTRACTING");
            task.setMessage("正在提取文档文本并准备分块...");
            task.setProgress(30);
            knowledgeImportTaskBiz.update(task);
            List<String> warnings = knowledgeBaseBiz.extractParsedContent(task.getDocId());
            if (warnings != null && !warnings.isEmpty()) {
                task.setMessage("文本提取完成，提示：" + String.join("；", warnings));
            } else {
                task.setMessage("文本提取完成");
            }
        }
        task.setStatus("VECTORIZING");
        task.setMessage("正在分块并向量化入库...");
        task.setProgress(60);
        knowledgeImportTaskBiz.update(task);

        knowledgeBaseBiz.processVectorize(task.getDocId());

        KnowledgeDoc doc = knowledgeDocService.getKnowledgeDocByDocId(task.getDocId());
        int chunkCount = doc == null || doc.getChunkCount() == null ? 0 : doc.getChunkCount();
        task.setStatus("COMPLETED");
        task.setMessage("解析入库完成");
        task.setProgress(100);
        task.setResourceId(task.getResourceId());
        knowledgeImportTaskBiz.update(task);
        log.info("知识库解析入库任务完成 docId={} chunkCount={}", task.getDocId(), chunkCount);
    }
}