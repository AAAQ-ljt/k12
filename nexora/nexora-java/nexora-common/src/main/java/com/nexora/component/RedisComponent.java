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
     * 同一用户只保留一条登录记录：先踢掉该用户旧会话，再写入新 token 与 userId -> token 映射
     */
    public void saveTokenInfo(String token, TokenUserInfoDTO tokenUserInfoDTO) {
        // 先清理该用户旧的登录记录（重复登录时旧 token 失效）
        removeUserToken(tokenUserInfoDTO.getUserId());

        // 写入新 token
        String key = Constants.REDIS_KEY_TOKEN + token;
        redisTemplate.opsForValue().set(key, tokenUserInfoDTO, 7, TimeUnit.DAYS);

        // 维护 userId -> token 映射，便于按用户清理
        redisTemplate.opsForValue().set(Constants.REDIS_KEY_USER_TOKEN + tokenUserInfoDTO.getUserId(),
                token, 7, TimeUnit.DAYS);
    }

    /**
     * 移除 Token（登录退出时调用，连带清理 userId -> token 映射）
     */
    public void removeToken(String token) {
        TokenUserInfoDTO userInfo = getTokenInfo(token);
        if (userInfo != null && userInfo.getUserId() != null) {
            redisTemplate.delete(Constants.REDIS_KEY_USER_TOKEN + userInfo.getUserId());
        }
        redisTemplate.delete(Constants.REDIS_KEY_TOKEN + token);
    }

    /**
     * 清理指定用户的全部登录记录（同一用户只保留一条）
     */
    public void removeUserToken(String userId) {
        if (userId == null || userId.isEmpty()) {
            return;
        }
        Object oldToken = redisTemplate.opsForValue().get(Constants.REDIS_KEY_USER_TOKEN + userId);
        if (oldToken != null) {
            redisTemplate.delete(Constants.REDIS_KEY_TOKEN + oldToken.toString());
        }
        redisTemplate.delete(Constants.REDIS_KEY_USER_TOKEN + userId);
    }
}
