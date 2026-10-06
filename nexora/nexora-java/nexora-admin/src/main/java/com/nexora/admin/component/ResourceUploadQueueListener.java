package com.nexora.admin.component;

import com.nexora.admin.service.ResourceUploadService;
import com.nexora.component.RedisComponent;
import com.nexora.component.ResourceZombieCleaner;
import com.nexora.constants.Constants;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Redis 异步队列消费者：轮询资源处理任务；并负责「处理中」僵尸资源清扫（启动自愈 + 定时）
 */
@Component
public class ResourceUploadQueueListener {

    private static final Logger log = LoggerFactory.getLogger(ResourceUploadQueueListener.class);

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private ResourceUploadService resourceUploadService;

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
            log.warn("管理端资源僵尸记录清扫失败", e);
        }
    }

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
