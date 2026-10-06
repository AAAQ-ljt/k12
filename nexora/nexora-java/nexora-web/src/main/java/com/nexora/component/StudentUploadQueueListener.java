package com.nexora.component;

import com.nexora.constants.Constants;
import com.nexora.service.StudentResourceUploadService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 学生个人资源异步处理队列消费者；并负责「处理中」僵尸资源清扫（启动自愈 + 定时）
 */
@Component
public class StudentUploadQueueListener {

    private static final Logger log = LoggerFactory.getLogger(StudentUploadQueueListener.class);

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private StudentResourceUploadService studentResourceUploadService;

    @Resource
    private ResourceZombieCleaner resourceZombieCleaner;

    /**
     * 启动自愈：上次进程被重启/杀死时中断在「处理中」的资源不会再有链路收敛，启动即清扫一轮
     */
    @PostConstruct
    public void init() {
        sweepZombies();
    }

    /**
     * 定时清扫：覆盖运行期产生的僵尸（用户中途放弃上传、队列消费异常中断等）
     */
    @Scheduled(fixedDelay = 600_000, initialDelay = 120_000)
    public void sweepZombies() {
        try {
            resourceZombieCleaner.cleanZombies();
        } catch (Exception e) {
            log.warn("学生端资源僵尸记录清扫失败", e);
        }
    }

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
