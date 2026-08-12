package com.nexora.admin.vo;

import com.nexora.entity.dto.TokenUserInfoDTO;

/**
 * 管理端登录返回 VO：token + 用户信息（对齐前端 { token, userInfo } 契约）
 */
public class AdminLoginVO {

    /**
     * 登录 Token（header adminToken）
     */
    private String token;

    /**
     * 登录用户信息
     */
    private TokenUserInfoDTO userInfo;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public TokenUserInfoDTO getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(TokenUserInfoDTO userInfo) {
        this.userInfo = userInfo;
    }
}
