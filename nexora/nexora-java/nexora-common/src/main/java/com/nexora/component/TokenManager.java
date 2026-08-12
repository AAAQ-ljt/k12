package com.nexora.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;

/**
 * 登录 Token 生成器：userId + secret 派生（不可预测，同一用户始终同一 token）
 * admin / web 两端共用，保证 Redis 里同一用户只有一条登录记录
 */
@Component
public class TokenManager {

    @Value("${login.token-secret:nexora-default-token-secret}")
    private String tokenSecret;

    /**
     * 生成登录 token
     */
    public String generateToken(String userId) {
        return sha256Hex(userId + tokenSecret);
    }

    /**
     * SHA-256 十六进制编码（JDK 自带）
     */
    private String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Token 生成失败", e);
        }
    }
}
