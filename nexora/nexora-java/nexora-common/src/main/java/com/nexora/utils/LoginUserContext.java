package com.nexora.utils;

import com.nexora.entity.dto.TokenUserInfoDTO;

/**
 * 当前登录用户上下文（ThreadLocal）
 * 拦截器登录校验通过后写入，Controller / Biz 从上下文取当前用户，禁止再解析 token
 */
public class LoginUserContext {

    private static final ThreadLocal<TokenUserInfoDTO> HOLDER = new ThreadLocal<>();

    private LoginUserContext() {
    }

    public static void set(TokenUserInfoDTO userInfo) {
        HOLDER.set(userInfo);
    }

    public static TokenUserInfoDTO get() {
        return HOLDER.get();
    }

    /**
     * 请求结束时清理，防止线程复用导致串号
     */
    public static void remove() {
        HOLDER.remove();
    }
}
