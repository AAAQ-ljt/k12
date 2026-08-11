package com.smart.campus.admin.task;

import com.alibaba.fastjson2.JSON;
import com.smart.campus.admin.biz.ResourceAdminBiz;
import com.smart.campus.redis.ResourceTaskQueueRedisComponent;
import com.smart.campus.entity.dto.ResourceQueueTaskDTO;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ResourceTaskConsumer {

    private static final Logger logger = LoggerFactory.getLogger(ResourceTaskConsumer.class);

    @Resource
    private ResourceTaskQueueRedisComponent resourceTaskQueueRedisComponent;

    @Resource
    private ResourceAdminBiz resourceAdminBiz;

    private ExecutorService executorService;

    @PostConstruct
    public void start() {
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                ResourceQueueTaskDTO task = null;
                try {
                    String message = resourceTaskQueueRedisComponent.take();
                    task = JSON.parseObject(message, ResourceQueueTaskDTO.class);
                    resourceAdminBiz.handleQueueTask(task);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    logger.error("处理资源异步任务失败", e);
                    resourceAdminBiz.markTaskFailed(task);
                }
            }
        });
    }

    @PreDestroy
    public void destroy() {
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }
}
