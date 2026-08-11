package com.smart.campus.entity.dto;

import jakarta.validation.constraints.NotBlank;

public class AdminLoginDTO {

    @NotBlank(message = "登录手机号不能为空")
    private String phone;

    @NotBlank(message = "登录密码不能为空")
    private String password;

    @NotBlank(message = "验证码标识不能为空")
    private String captchaKey;

    @NotBlank(message = "图片验证码不能为空")
    private String captchaCode;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCaptchaKey() {
        return captchaKey;
    }

    public void setCaptchaKey(String captchaKey) {
        this.captchaKey = captchaKey;
    }

    public String getCaptchaCode() {
        return captchaCode;
    }

    public void setCaptchaCode(String captchaCode) {
        this.captchaCode = captchaCode;
    }
}
