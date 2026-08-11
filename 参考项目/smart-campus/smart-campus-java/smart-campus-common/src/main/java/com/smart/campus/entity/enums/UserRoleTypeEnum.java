package com.smart.campus.entity.enums;

public enum UserRoleTypeEnum {

    ADMIN(0, "管理员"),
    TEACHER(1, "老师"),
    STUDENT(2, "学生");

    private final Integer code;

    private final String desc;

    UserRoleTypeEnum(Integer code, String desc) {
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
