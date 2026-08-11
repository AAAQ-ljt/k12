package com.smart.campus.entity.enums;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public enum GenderEnum {

    UNKNOWN(0, "未知"),
    MALE(1, "男"),
    FEMALE(2, "女");

    private final Integer code;

    private final String desc;

    GenderEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static Map<Integer, String> toMap() {
        Map<Integer, String> map = new LinkedHashMap<>();
        Arrays.stream(values()).forEach(item -> map.put(item.getCode(), item.getDesc()));
        return map;
    }
}
