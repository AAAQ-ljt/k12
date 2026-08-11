package com.easymall.entity.enums;

import java.util.Arrays;
import java.util.Optional;

public enum OrderFromTypeEnum {
    PRODUCT(0, "商品页"),
    CART(1, "购物车");

    private Integer type;
    private String desc;

    OrderFromTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }


    public static OrderFromTypeEnum getByType(Integer type) {
        Optional<OrderFromTypeEnum> typeEnum = Arrays.stream(OrderFromTypeEnum.values()).filter(value -> value.getType().equals(type)).findFirst();
        return typeEnum == null || typeEnum.isEmpty() ? null : typeEnum.get();
    }
}
