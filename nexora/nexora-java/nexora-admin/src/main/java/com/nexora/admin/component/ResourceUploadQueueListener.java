package com.nexora.admin.component;

import com.nexora.admin.service.ResourceUploadService;
import com.nexora.component.RedisComponent;
import com.nexora.constants.Constants;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Redis 异步队列消费者：轮询资源处理任务
 */
@Component
public class ResourceUploadQueueListener {

    private static final Logger log = LoggerFactory.getLogger(ResourceUploadQueueListener.class);

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private ResourceUploadService resourceUploadService;

    @Scheduled(fixedDelay = 1000)
    public void consume() {
        Object task = redisComponent.rightPop(Constants.REDIS_KEY_RESOURCE_UPLOAD_QUEUE);
        if (task == null) {
            return;
        }
        String uploadId = task.toString();
        try {
            resourceUploadService.process(uploadId);
        } catch (Exception e) {
            log.error("资源异步处理异常 uploadId={}", uploadId, e);
        }
    }
}
