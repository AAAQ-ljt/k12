package com.nexora.vo;

/**
 * 图形验证码返回
 */
public class CheckCodeVO {

    /**
     * 验证码图片 Base64（前端直接作 img src）
     */
    private String checkCodeBase64;

    /**
     * 验证码 key（提交登录 / 注册时原样带回）
     */
    private String checkCodeKey;

    public CheckCodeVO() {
    }

    public CheckCodeVO(String checkCodeBase64, String checkCodeKey) {
        this.checkCodeBase64 = checkCodeBase64;
        this.checkCodeKey = checkCodeKey;
    }

    public String getCheckCodeBase64() {
        return checkCodeBase64;
    }

    public void setCheckCodeBase64(String checkCodeBase64) {
        this.checkCodeBase64 = checkCodeBase64;
    }

    public String getCheckCodeKey() {
        return checkCodeKey;
    }

    public void setCheckCodeKey(String checkCodeKey) {
        this.checkCodeKey = checkCodeKey;
    }
}
