package com.smart.campus.entity.enums;

public enum ResourceNodeTypeEnum {

    FOLDER(1, "目录"),
    RESOURCE(2, "资源");

    private final Integer code;

    private final String desc;

    ResourceNodeTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
