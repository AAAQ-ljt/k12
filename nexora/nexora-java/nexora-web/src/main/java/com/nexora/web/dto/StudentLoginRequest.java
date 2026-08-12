package com.nexora.web.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 学生端登录请求 DTO
 */
@Data
public class StudentLoginRequest implements Serializable {

    /**
     * 邮箱
     */
    private String email;

    /**
     * 明文密码（登录后 MD5 加密比对）
     */
    private String password;
}
