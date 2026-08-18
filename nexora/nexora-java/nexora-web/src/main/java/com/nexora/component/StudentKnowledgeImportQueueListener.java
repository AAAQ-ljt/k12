package com.nexora.component;

import com.nexora.constants.Constants;
import com.nexora.entity.po.KnowledgeDoc;
import com.nexora.entity.po.ResourceInfo;
import com.nexora.service.KnowledgeDocService;
import com.nexora.service.ResourceInfoService;
import com.nexora.utils.StringTools;
import com.nexora.utils.TextChunker;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 学生个人知识库异步队列消费者：文档解析与向量化入库。
 */
@Component
public class StudentKnowledgeImportQueueListener {

    private static final Logger log = LoggerFactory.getLogger(StudentKnowledgeImportQueueListener.class);

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private KnowledgeDocService knowledgeDocService;

    @Resource
    private ResourceInfoService resourceInfoService;

    @Resource
    private ResourceKnowledgeParser resourceKnowledgeParser;

    @Resource
    private KnowledgeVectorComponent knowledgeVectorComponent;

    @Scheduled(fixedDelay = 1000)
    public void consume() {
        Object task = redisComponent.rightPop(Constants.REDIS_KEY_STUDENT_KNOWLEDGE_QUEUE);
        if (task == null) {
            return;
        }
        String docId = task.toString();
        try {
            processKnowledgeDoc(docId);
        } catch (Exception e) {
            log.error("学生知识文档解析入库异常 docId={}", docId, e);
            markFailed(docId, e.getMessage() == null ? "解析入库失败" : e.getMessage());
        }
    }

    private void processKnowledgeDoc(String docId) {
        if (StringTools.isEmpty(docId)) {
            return;
        }
        KnowledgeDoc doc = knowledgeDocService.getKnowledgeDocByDocId(docId);
        if (doc == null || StringTools.isEmpty(doc.getSourceResourceId())) {
            return;
        }
        markProcessing(docId);
        try {
            ResourceInfo resource = resourceInfoService.getResourceInfoByResourceId(doc.getSourceResourceId());
            if (resource == null || resource.getStatus() == null || resource.getStatus() != 1) {
                throw new IllegalArgumentException("源资源不存在或暂不可用");
            }
            ResourceKnowledgeParser.ParseResult parsed = resourceKnowledgeParser.parse(resource);

            KnowledgeDoc contentUpdate = new KnowledgeDoc();
            contentUpdate.setContent(parsed.getText());
            contentUpdate.setUpdateTime(new Date());
            knowledgeDocService.updateKnowledgeDocByDocId(contentUpdate, docId);

            KnowledgeDoc fresh = knowledgeDocService.getKnowledgeDocByDocId(docId);
            List<String> chunks = TextChunker.split(fresh.getContent(), 500);
            if (chunks.isEmpty()) {
                throw new IllegalArgumentException("文档分块结果为空");
            }
            int oldCount = fresh.getChunkCount() == null ? 0 : fresh.getChunkCount();
            knowledgeVectorComponent.deleteChunks(docId, Math.max(oldCount, chunks.size()));
            knowledgeVectorComponent.saveChunks(docId, fresh.getTitle(), fresh.getStage(),
                    fresh.getKnowledgePointId(), fresh.getDifficulty(), fresh.getSourceUrl(),
                    fresh.getSourceResourceId(), fresh.getOwnerId(), chunks);

            KnowledgeDoc done = new KnowledgeDoc();
            done.setVectorStatus(2);
            done.setVectorError(null);
            done.setChunkCount(chunks.size());
            done.setUpdateTime(new Date());
            knowledgeDocService.updateKnowledgeDocByDocId(done, docId);
            log.info("学生知识文档入库完成 docId={} chunks={}", docId, chunks.size());
        } catch (Exception e) {
            log.warn("学生知识文档解析失败 docId={}", docId, e);
            markFailed(docId, e.getMessage() == null ? "解析入库失败" : e.getMessage());
        }
    }

    private void markProcessing(String docId) {
        KnowledgeDoc update = new KnowledgeDoc();
        update.setVectorStatus(1);
        update.setVectorError(null);
        update.setUpdateTime(new Date());
        knowledgeDocService.updateKnowledgeDocByDocId(update, docId);
    }

    private void markFailed(String docId, String error) {
        KnowledgeDoc update = new KnowledgeDoc();
        update.setVectorStatus(3);
        update.setVectorError(error);
        update.setUpdateTime(new Date());
        knowledgeDocService.updateKnowledgeDocByDocId(update, docId);
    }
}
