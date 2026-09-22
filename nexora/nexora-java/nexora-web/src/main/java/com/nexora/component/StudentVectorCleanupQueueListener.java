package com.nexora.component;

import com.nexora.constants.Constants;
import com.nexora.entity.po.KnowledgeDoc;
import com.nexora.service.KnowledgeDocService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 学生个人知识页向量清理队列消费者：
 * nexora-mcp 没有向量库，知识页被覆盖/删除时把清理任务投递到本队列，由 web 端执行 ES chunk 删除。
 */
@Component
public class StudentVectorCleanupQueueListener {

    private static final Logger log = LoggerFactory.getLogger(StudentVectorCleanupQueueListener.class);

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private KnowledgeDocService knowledgeDocService;

    @Resource
    private KnowledgeVectorComponent knowledgeVectorComponent;

    @Scheduled(fixedDelay = 1000)
    public void consume() {
        Object task = redisComponent.rightPop(Constants.REDIS_KEY_STUDENT_KNOWLEDGE_CLEANUP_QUEUE);
        if (task == null) {
            return;
        }
        String docId = task.toString();
        try {
            KnowledgeDoc doc = knowledgeDocService.getKnowledgeDocByDocId(docId);
            int chunkCount = doc == null || doc.getChunkCount() == null ? 0 : doc.getChunkCount();
            // 按分块数清理（方法内部有 200 个 ID 的保底范围，知识页已删除时也能清掉残留）
            knowledgeVectorComponent.deleteChunks(docId, chunkCount);
            if (doc != null && doc.getChunkCount() != null && doc.getChunkCount() > 0) {
                KnowledgeDoc update = new KnowledgeDoc();
                update.setChunkCount(0);
                update.setUpdateTime(new Date());
                knowledgeDocService.updateKnowledgeDocByDocId(update, docId);
            }
            log.info("知识页向量清理完成 docId={} chunks={}", docId, chunkCount);
        } catch (Exception e) {
            log.warn("知识页向量清理失败 docId={}", docId, e);
        }
    }
}
