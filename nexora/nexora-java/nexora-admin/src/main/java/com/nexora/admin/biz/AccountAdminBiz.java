package com.nexora.admin.biz;

import com.nexora.component.RedisComponent;
import com.nexora.admin.vo.AdminLoginVO;
import com.nexora.entity.dto.TokenUserInfoDTO;
import com.nexora.exception.BusinessException;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;

/**
 * 管理员账号管理 Biz
 */
@Component
public class AccountAdminBiz {

    @Value("${admin.account.username}")
    private String adminUsername;

    @Value("${admin.account.password}")
    private String adminPassword;

    @Value("${admin.account.email:admin@nexora.com}")
    private String adminEmail;

    @Value("${login.token-secret:nexora-default-token-secret}")
    private String tokenSecret;

    @Resource
    private RedisComponent redisComponent;

    /**
     * 管理员登录
     * 
     * @param username 用户名
     * @param password 密码（明文）
     * @return AdminLoginVO（token + userInfo，对齐前端契约）
     */
    public AdminLoginVO login(String username, String password) {
        // 1. 验证账号密码
        if (!adminUsername.equals(username)) {
            throw new BusinessException("用户名错误");
        }
        
        // MD5 加密后比对
        String md5Password = md5(password);
        if (!md5Password.equals(adminPassword)) {
            throw new BusinessException("密码错误");
        }

        // 2. 生成 Token（由 userId 派生，同一用户重复登录覆盖同一条 Redis 记录）
        String token = generateToken("admin_001");

        // 3. 构建用户信息
        TokenUserInfoDTO userInfo = new TokenUserInfoDTO();
        userInfo.setToken(token);
        userInfo.setUsername(username);
        userInfo.setEmail(adminEmail);
        userInfo.setRoleType(0); // 0-管理员
        userInfo.setUserId("admin_001");

        // 4. 保存 Token 到 Redis（7 天过期）
        redisComponent.saveTokenInfo(token, userInfo);

        // 5. 组装返回（token + userInfo）
        AdminLoginVO loginVO = new AdminLoginVO();
        loginVO.setToken(token);
        loginVO.setUserInfo(userInfo);
        return loginVO;
    }

    /**
     * 根据 Token 获取当前登录管理员信息
     */
    public TokenUserInfoDTO getLoginInfo(String token) {
        TokenUserInfoDTO userInfo = redisComponent.getTokenInfo(token);
        if (userInfo == null) {
            throw new BusinessException("登录已失效，请重新登录");
        }
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
     * 生成 Token：userId + secret 派生（不可预测，同一用户始终同一 token）
     */
    private String generateToken(String userId) {
        return sha256Hex(userId + tokenSecret);
    }

    /**
     * SHA-256 十六进制编码（JDK 自带，避免额外依赖）
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
