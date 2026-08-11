package com.smart.campus.entity.enums;

import java.util.Arrays;
import java.util.List;

public enum TeacherTitleEnum {

    ASSISTANT("助教", "助教"),
    LECTURER("讲师", "讲师"),
    ASSOCIATE_PROFESSOR("副教授", "副教授"),
    PROFESSOR("教授", "教授");

    private final String code;

    private final String desc;

    TeacherTitleEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static List<TeacherTitleEnum> getAll() {
        return Arrays.asList(values());
    }
}
