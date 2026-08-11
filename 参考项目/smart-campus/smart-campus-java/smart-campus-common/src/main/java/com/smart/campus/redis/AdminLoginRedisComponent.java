package com.smart.campus.redis;

import com.smart.campus.entity.constants.RedisKeyConstants;
import com.smart.campus.entity.vo.LoginUserVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class AdminLoginRedisComponent {

    private static final Duration LOGIN_TTL = Duration.ofDays(7);
    private static final Duration CAPTCHA_TTL = Duration.ofMinutes(5);

    @Resource
    private WebLoginRedisComponent.RedisUtils redisUtils;

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
        return RedisKeyConstants.buildKey(RedisKeyConstants.ADMIN_LOGIN_TOKEN_PREFIX, token);
    }

    private String buildCaptchaKey(String captchaKey) {
        return RedisKeyConstants.buildKey(RedisKeyConstants.ADMIN_LOGIN_CAPTCHA_PREFIX, captchaKey);
    }
}
