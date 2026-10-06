package com.nexora.entity.vo;

/**
 * 编程题判分结果（预期值不下发前端，只回结论与提示文案）
 */
public class CodingJudgeResultVO {

    private Boolean passed;

    /** 给学生的提示（通过/不通过各自一句，不泄露期望输出原文） */
    private String message;

    public CodingJudgeResultVO() {
    }

    public CodingJudgeResultVO(Boolean passed, String message) {
        this.passed = passed;
        this.message = message;
    }

    public Boolean getPassed() {
        return passed;
    }

    public void setPassed(Boolean passed) {
        this.passed = passed;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
