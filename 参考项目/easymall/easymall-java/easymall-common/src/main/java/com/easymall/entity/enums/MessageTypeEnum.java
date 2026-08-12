package com.easymall.entity.enums;

public enum MessageTypeEnum {
    USER(0, "用户发送"), ASSISTANT(1, "AI回答");
    private Integer type;
    private String desc;

    MessageTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
