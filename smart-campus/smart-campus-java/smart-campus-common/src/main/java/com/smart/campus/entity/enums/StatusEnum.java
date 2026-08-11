package com.smart.campus.entity.enums;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public enum StatusEnum {

    ENABLED(1, "启用"),
    DISABLED(0, "停用");

    private final Integer code;

    private final String desc;

    StatusEnum(Integer code, String desc) {
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
