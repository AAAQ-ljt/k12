package com.smart.campus.utils;

import com.smart.campus.entity.vo.LoginUserVO;

public class LoginUserContextHolder {

    private static final ThreadLocal<LoginUserVO> CONTEXT = new ThreadLocal<>();

    private LoginUserContextHolder() {
    }

    public static void set(LoginUserVO loginUser) {
        CONTEXT.set(loginUser);
    }

    public static LoginUserVO get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
