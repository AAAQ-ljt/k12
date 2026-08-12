package com.nexora.vo;

import com.nexora.entity.dto.TokenUserInfoDTO;

/**
 * 学生登录返回（对齐前端 { token, userInfo } 契约）
 */
public class StudentLoginVO {

    private String token;

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
