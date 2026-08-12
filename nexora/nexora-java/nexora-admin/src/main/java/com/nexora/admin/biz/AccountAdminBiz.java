package com.nexora.admin.biz;

import com.nexora.component.RedisComponent;
import com.nexora.entity.dto.TokenUserInfoDTO;
import com.nexora.exception.BusinessException;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.util.Random;

/**
 * 管理员账号管理 Biz
 */
@Component
public class AccountAdminBiz {

    @Value("${admin.account.username}")
    private String adminUsername;

    @Value("${admin.account.password}")
    private String adminPassword;

    @Resource
    private RedisComponent redisComponent;

    /**
     * 管理员登录
     * 
     * @param username 用户名
     * @param password 密码（明文）
     * @return TokenUserInfoDTO（包含 token）
     */
    public TokenUserInfoDTO login(String username, String password) {
        // 1. 验证账号密码
        if (!adminUsername.equals(username)) {
            throw new BusinessException("用户名错误");
        }
        
        // MD5 加密后比对
        String md5Password = md5(password);
        if (!md5Password.equals(adminPassword)) {
            throw new BusinessException("密码错误");
        }

        // 2. 生成 Token（32 位随机字符串）
        String token = generateToken();

        // 3. 构建用户信息
        TokenUserInfoDTO userInfo = new TokenUserInfoDTO();
        userInfo.setToken(token);
        userInfo.setUsername(username);
        userInfo.setRoleType(0); // 0-管理员
        userInfo.setUserId("admin_001");

        // 4. 保存 Token 到 Redis（7 天过期）
        redisComponent.saveTokenInfo(token, userInfo);

        return userInfo;
    }

    /**
     * 退出登录
     * 
     * @param token 管理端 Token
     */
    public void logout(String token) {
        // 删除 Redis 中的 Token
        redisComponent.removeToken(token);
    }

    /**
     * 生成 MD5 哈希
     */
    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5 加密失败", e);
        }
    }

    /**
     * 生成随机 Token（32 位）
     */
    private String generateToken() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(32);
        for (int i = 0; i < 32; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
