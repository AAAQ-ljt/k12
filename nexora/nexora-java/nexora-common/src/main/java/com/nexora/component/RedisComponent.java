package com.nexora.component;

import com.alibaba.fastjson2.JSON;
import com.nexora.constants.Constants;
import com.nexora.entity.dto.TokenUserInfoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 操作组件
 */
@Component
public class RedisComponent {

    @Autowired
    private org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate;

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
        Object tokenInfo = redisTemplate.opsForValue().get(key);
        if (tokenInfo == null) {
            return null;
        }
        // Jackson 反序列化返回的是 Map/String，统一转成 JSON 字符串再解析为 DTO
        String json = tokenInfo instanceof String stringValue ? stringValue : JSON.toJSONString(tokenInfo);
        return JSON.parseObject(json, TokenUserInfoDTO.class);
    }

    /**
     * 保存 Token 信息
     */
    public void saveTokenInfo(String token, TokenUserInfoDTO tokenUserInfoDTO) {
        String key = Constants.REDIS_KEY_TOKEN + token;
        redisTemplate.opsForValue().set(key, tokenUserInfoDTO,
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
