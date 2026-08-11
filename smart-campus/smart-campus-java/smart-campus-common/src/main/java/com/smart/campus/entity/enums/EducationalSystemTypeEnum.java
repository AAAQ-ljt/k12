package com.smart.campus.entity.enums;

import java.util.Arrays;
import java.util.List;

public enum EducationalSystemTypeEnum {

    THREE_YEAR(3, "3年"),
    FOUR_YEAR(4, "4年");

    private final Integer code;

    private final String desc;

    EducationalSystemTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static List<EducationalSystemTypeEnum> getAll() {
        return Arrays.asList(values());
    }
}
