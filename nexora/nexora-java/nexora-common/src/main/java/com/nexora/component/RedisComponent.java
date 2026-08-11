package com.nexora.component;

import com.alibaba.fastjson2.JSON;
import com.nexora.constants.Constants;
import com.nexora.entity.dto.TokenUserInfoDTO;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 操作组件
 */
@Component
public class RedisComponent {

    @Resource
    private StringRedisTemplate redisTemplate;

    /**
     * 保存用户心跳
     */
    public void saveUserHeartBeat(String userId) {
        String key = Constants.REDIS_KEY_HEART_BEAT + userId;
        redisTemplate.opsForValue().set(key, String.valueOf(System.currentTimeMillis()),
                Constants.HEART_BEAT_TIMEOUT, TimeUnit.SECONDS);
    }

    /**
     * 获取 Token 信息
     */
    public TokenUserInfoDTO getTokenInfo(String token) {
        String key = Constants.REDIS_KEY_TOKEN + token;
        String tokenInfo = redisTemplate.opsForValue().get(key);
        if (tokenInfo == null) {
            return null;
        }
        return JSON.parseObject(tokenInfo, TokenUserInfoDTO.class);
    }

    /**
     * 保存 Token 信息
     */
    public void saveTokenInfo(String token, TokenUserInfoDTO tokenUserInfoDTO) {
        String key = Constants.REDIS_KEY_TOKEN + token;
        redisTemplate.opsForValue().set(key, JSON.toJSONString(tokenUserInfoDTO),
                7, TimeUnit.DAYS);
    }

    /**
     * 移除 Token
     */
    public void removeToken(String token) {
        String key = Constants.REDIS_KEY_TOKEN + token;
        redisTemplate.delete(key);
    }
}
