package com.easymall.entity.enums;


public enum AgentMessageStatusEnum {
    CANCEL(0, "取消"),
    NORMAL(1, "正常"),
    COMPLETE(2, "完成");

    private Integer status;

    private String desc;

    AgentMessageStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }
}
