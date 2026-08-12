package com.nexora.entity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Token 用户信息 DTO
 */
@Data
public class TokenUserInfoDTO implements Serializable {

    private String userId;

    private String username;

    /**
     * Token 值
     */
    private String token;

    /**
     * 学段：PRIMARY_LOW / PRIMARY_HIGH / JUNIOR / SENIOR
     */
    private String stage;

    /**
     * 角色类型：0 管理员 / 1 学生
     */
    private Integer roleType;

    /**
     * 邮箱（登录核心字段，管理员可为空）
     */
    private String email;

    /**
     * 头像 URL
     */
    private String avatar;
}
