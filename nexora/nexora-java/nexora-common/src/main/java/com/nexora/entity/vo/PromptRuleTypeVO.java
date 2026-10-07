package com.nexora.entity.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户提示词规则的「类型选项」（计划 C2，学生端下拉用）。
 */
@Data
public class PromptRuleTypeVO implements Serializable {

    /** 类型码：CALL_ME / LENGTH / STYLE / DIFFICULTY / EXAMPLE / LANGUAGE / FORBID */
    private String code;

    /** 展示名，如「怎么称呼我」 */
    private String name;
}
