package com.nexora.web.vo;

import com.nexora.entity.dto.TokenUserInfoDTO;
import lombok.Data;

import java.io.Serializable;

/**
 * 学生端登录响应 VO
 */
@Data
public class StudentLoginVO implements Serializable {

    /**
     * 登录令牌
     */
    private String token;

    /**
     * 用户信息
     */
    private TokenUserInfoDTO userInfo;
}
