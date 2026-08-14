package com.nexora.admin.component;

import com.nexora.admin.biz.KnowledgeBaseBiz;
import com.nexora.component.RedisComponent;
import com.nexora.constants.Constants;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Redis 异步队列消费者：轮询知识库解析入库任务。
 */
@Component
public class KnowledgeImportQueueListener {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeImportQueueListener.class);

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private KnowledgeBaseBiz knowledgeBaseBiz;

    @Scheduled(fixedDelay = 1000)
    public void consume() {
        Object task = redisComponent.rightPop(Constants.REDIS_KEY_KNOWLEDGE_IMPORT_QUEUE);
        if (task == null) {
            return;
        }
        String docId = task.toString();
        try {
            knowledgeBaseBiz.processKnowledgeImport(docId);
        } catch (Exception e) {
            log.error("知识库解析入库异常 docId={}", docId, e);
        }
    }
}
