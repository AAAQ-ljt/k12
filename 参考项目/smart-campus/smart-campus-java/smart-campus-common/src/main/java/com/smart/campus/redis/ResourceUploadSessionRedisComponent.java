package com.smart.campus.redis;

import com.smart.campus.entity.constants.RedisKeyConstants;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class ResourceUploadSessionRedisComponent {

    @Resource
    private WebLoginRedisComponent.RedisUtils redisUtils;

    public void save(String uploadId, Object session, Duration duration) {
        redisUtils.setJson(buildKey(uploadId), session, duration);
    }

    public <T> T get(String uploadId, Class<T> clazz) {
        return redisUtils.getJson(buildKey(uploadId), clazz);
    }

    public void delete(String uploadId) {
        redisUtils.delete(buildKey(uploadId));
    }

    private String buildKey(String uploadId) {
        return RedisKeyConstants.buildKey(RedisKeyConstants.RESOURCE_UPLOAD_SESSION_PREFIX, uploadId);
    }
}
