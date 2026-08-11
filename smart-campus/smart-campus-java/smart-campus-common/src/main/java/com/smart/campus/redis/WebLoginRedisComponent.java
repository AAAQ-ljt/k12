package com.smart.campus.redis;

import com.alibaba.fastjson2.JSON;
import com.smart.campus.entity.constants.RedisKeyConstants;
import com.smart.campus.entity.vo.LoginUserVO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;

@Component
public class WebLoginRedisComponent {

    private static final Duration LOGIN_TTL = Duration.ofDays(7);
    private static final Duration CAPTCHA_TTL = Duration.ofMinutes(5);

    @Resource
    private RedisUtils redisUtils;

    public void save(String token, LoginUserVO loginUser) {
        redisUtils.setJson(buildKey(token), loginUser, LOGIN_TTL);
    }

    public LoginUserVO get(String token) {
        return redisUtils.getJson(buildKey(token), LoginUserVO.class);
    }

    public void delete(String token) {
        redisUtils.delete(buildKey(token));
    }

    public void saveCaptcha(String captchaKey, String captchaCode) {
        redisUtils.set(buildCaptchaKey(captchaKey), captchaCode, CAPTCHA_TTL);
    }

    public String getCaptcha(String captchaKey) {
        return redisUtils.get(buildCaptchaKey(captchaKey));
    }

    public void deleteCaptcha(String captchaKey) {
        redisUtils.delete(buildCaptchaKey(captchaKey));
    }

    private String buildKey(String token) {
        return RedisKeyConstants.buildKey(RedisKeyConstants.WEB_LOGIN_TOKEN_PREFIX, token);
    }

    private String buildCaptchaKey(String captchaKey) {
        return RedisKeyConstants.buildKey(RedisKeyConstants.WEB_LOGIN_CAPTCHA_PREFIX, captchaKey);
    }

    @Component
    public static class RedisUtils {

        @Resource
        private StringRedisTemplate stringRedisTemplate;

        @Resource
        private RedissonClient redissonClient;

        public void set(String key, String value) {
            stringRedisTemplate.opsForValue().set(key, value);
        }

        public void set(String key, String value, Duration duration) {
            stringRedisTemplate.opsForValue().set(key, value, duration);
        }

        public String get(String key) {
            return stringRedisTemplate.opsForValue().get(key);
        }

        public void setJson(String key, Object value, Duration duration) {
            if (value == null) {
                return;
            }
            set(key, JSON.toJSONString(value), duration);
        }

        public <T> T getJson(String key, Class<T> clazz) {
            String json = get(key);
            if (StringUtils.isBlank(json)) {
                return null;
            }
            return JSON.parseObject(json, clazz);
        }

        public Boolean hasKey(String key) {
            return stringRedisTemplate.hasKey(key);
        }

        public Boolean expire(String key, Duration duration) {
            return stringRedisTemplate.expire(key, duration);
        }

        public Boolean delete(String key) {
            return stringRedisTemplate.delete(key);
        }

        public Long addSetMembers(String key, String... values) {
            return stringRedisTemplate.opsForSet().add(key, values);
        }

        public Set<String> getSetMembers(String key) {
            Set<String> members = stringRedisTemplate.opsForSet().members(key);
            return members == null ? Collections.emptySet() : members;
        }

        public Long removeSetMembers(String key, String... values) {
            return stringRedisTemplate.opsForSet().remove(key, (Object[]) values);
        }

        public void offerQueue(String queueKey, String message) {
            redissonClient.getBlockingQueue(queueKey).offer(message);
        }

        public String takeQueue(String queueKey) throws InterruptedException {
            RBlockingQueue<String> queue = redissonClient.getBlockingQueue(queueKey);
            return queue.take();
        }
    }
}
