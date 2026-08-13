package com.nexora.component;

import com.alibaba.fastjson2.JSON;
import com.nexora.constants.Constants;
import com.nexora.entity.dto.TokenUserInfoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.Set;
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
     * token 由 userId 派生（见 AccountAdminBiz.generateToken），同一用户重复登录会覆盖同一条记录
     */
    public void saveTokenInfo(String token, TokenUserInfoDTO tokenUserInfoDTO) {
        String key = Constants.REDIS_KEY_TOKEN + token;
        redisTemplate.opsForValue().set(key, tokenUserInfoDTO, 7, TimeUnit.DAYS);
    }

    /**
     * 移除 Token（登录退出时调用）
     */
    public void removeToken(String token) {
        redisTemplate.delete(Constants.REDIS_KEY_TOKEN + token);
    }

    /**
     * 保存图形验证码（10 分钟有效），返回验证码 key
     */
    public String saveCheckCode(String code, String oldCheckCodeKey) {
        cleanAllCheckCode();
        cleanCheckCode(oldCheckCodeKey);
        String checkCodeKey = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey, code,
                Constants.CHECK_CODE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        return checkCodeKey;
    }

    /**
     * 清理所有图形验证码（刷新前调用，避免历史 key 残留堆积）
     */
    private void cleanAllCheckCode() {
        Set<String> keys = redisTemplate.keys(Constants.REDIS_KEY_CHECK_CODE + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 获取图形验证码
     */
    public String getCheckCode(String checkCodeKey) {
        if (checkCodeKey == null || checkCodeKey.isEmpty()) {
            return null;
        }
        Object code = redisTemplate.opsForValue().get(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
        return code == null ? null : code.toString();
    }

    /**
     * 清除图形验证码（登录/注册后无论成败都清理，防止复用）
     */
    public void cleanCheckCode(String checkCodeKey) {
        if (checkCodeKey != null && !checkCodeKey.isEmpty()) {
            redisTemplate.delete(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
        }
    }

    /**
     * 保存 AI 消息取消标记（取消后异步流式任务会尽快停止）
     */
    public void saveCancelMessage(String userId, String messageId) {
        String key = Constants.REDIS_KEY_AI_CANCEL + userId + ":" + messageId;
        redisTemplate.opsForValue().set(key, "1", 30, TimeUnit.MINUTES);
    }

    /**
     * 判断 AI 消息是否已被取消
     */
    public boolean hasCancelMessage(String userId, String messageId) {
        String key = Constants.REDIS_KEY_AI_CANCEL + userId + ":" + messageId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 清除 AI 消息取消标记
     */
    public void removeCancelMessage(String userId, String messageId) {
        String key = Constants.REDIS_KEY_AI_CANCEL + userId + ":" + messageId;
        redisTemplate.delete(key);
    }

    /**
     * 读取字符串值（提示词等覆盖配置）
     */
    public String getString(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return value == null ? null : value.toString();
    }

    /**
     * 写入字符串值（提示词等覆盖配置）
     */
    public void setString(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }
}
