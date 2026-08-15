package com.nexora.component;

import com.nexora.constants.Constants;
import com.nexora.service.StudentResourceUploadService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 学生个人资源异步处理队列消费者
 */
@Component
public class StudentUploadQueueListener {

    private static final Logger log = LoggerFactory.getLogger(StudentUploadQueueListener.class);

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private StudentResourceUploadService studentResourceUploadService;

    @Scheduled(fixedDelay = 1000)
    public void consume() {
        Object task = redisComponent.rightPop(Constants.REDIS_KEY_STUDENT_RESOURCE_UPLOAD_QUEUE);
        if (task == null) {
            return;
        }
        String uploadId = task.toString();
        try {
            studentResourceUploadService.process(uploadId);
        } catch (Exception e) {
            log.error("学生资源异步处理异常 uploadId={}", uploadId, e);
        }
    }
}
