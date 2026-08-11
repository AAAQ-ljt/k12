package com.smart.campus.redis;

import com.smart.campus.entity.constants.RedisKeyConstants;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class ResourceTaskQueueRedisComponent {

    @Resource
    private WebLoginRedisComponent.RedisUtils redisUtils;

    public void offer(String message) {
        redisUtils.offerQueue(RedisKeyConstants.RESOURCE_TASK_QUEUE_KEY, message);
    }

    public String take() throws InterruptedException {
        return redisUtils.takeQueue(RedisKeyConstants.RESOURCE_TASK_QUEUE_KEY);
    }
}
