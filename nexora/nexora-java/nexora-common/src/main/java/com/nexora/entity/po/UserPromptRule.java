package com.nexora.entity.po;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户端提示词规则（计划 C2）：学生自定义的回答偏好，拼进系统提示词的第四层（优先级低于平台规则）。
 */
@Data
public class UserPromptRule implements Serializable {

    /** 规则 ID */
    private Long ruleId;

    /** 学生 ID */
    private String userId;

    /** 类型：CALL_ME/LENGTH/STYLE/DIFFICULTY/EXAMPLE/LANGUAGE/FORBID */
    private String ruleType;

    /** 规则内容（学生原话） */
    private String ruleValue;

    /** 生效范围：GLOBAL / SCENE（预留） */
    private String scope;

    /** 状态：1启用 0停用 */
    private Integer status;

    /** 来源：STUDENT / AI_SUGGEST / SYSTEM / ADMIN */
    private String source;

    private Date createTime;

    private Date updateTime;
}
