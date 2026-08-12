package com.easymall.entity.enums;

import java.util.Arrays;
import java.util.Optional;

public enum PayChannelEnum {
    ALIPAY_PC("alipay", "alipay_pc", "payChannel4Alipay", "支付宝扫码支付");

    private String payChannel;
    private String payScene;
    private String beanName;
    private String desc;

    PayChannelEnum(String payChannel, String payScene, String beanName, String desc) {
        this.payChannel = payChannel;
        this.payScene = payScene;
        this.beanName = beanName;
        this.desc = desc;
    }

    public String getPayChannel() {
        return payChannel;
    }

    public String getPayScene() {
        return payScene;
    }

    public String getBeanName() {
        return beanName;
    }

    public String getDesc() {
        return desc;
    }

    public static PayChannelEnum getByPayScene(String payScene) {
        Optional<PayChannelEnum> typeEnum = Arrays.stream(PayChannelEnum.values()).filter(value -> value.getPayScene().equals(payScene)).findFirst();
        return typeEnum == null || typeEnum.isEmpty() ? null : typeEnum.get();
    }
}
