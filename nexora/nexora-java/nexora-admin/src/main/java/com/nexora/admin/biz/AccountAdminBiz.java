package com.nexora.admin.biz;

import com.nexora.admin.vo.AdminLoginVO;
import com.nexora.component.RedisComponent;
import com.nexora.component.TokenManager;
import com.nexora.entity.dto.TokenUserInfoDTO;
import com.nexora.exception.BusinessException;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private TokenManager tokenManager;

    /**
     * 管理员登录
     *
     * @param username 用户名
     * @param password 密码（明文）
     * @return AdminLoginVO（token + userInfo，对齐前端契约）
     */
    public AdminLoginVO login(String username, String password) {
        // 1. 验证账号密码（密码 MD5 比对）
        if (!adminUsername.equals(username)) {
            throw new BusinessException("用户名错误");
        }
        if (!StringTools.encodeByMD5(password).equals(adminPassword)) {
            throw new BusinessException("密码错误");
        }

        // 2. 生成 Token（由 userId 派生，同一用户重复登录覆盖同一条 Redis 记录）
        String token = tokenManager.generateToken("admin_001");

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
        redisComponent.removeToken(token);
    }
}
