package com.nexora.entity.enums;

/**
 * 学段枚举（冗余在 user_info.stage，全站按学段切换）
 */
public enum StageEnum {

    PRIMARY_LOW("PRIMARY_LOW", "小学低年级"),
    PRIMARY_HIGH("PRIMARY_HIGH", "小学高年级"),
    JUNIOR("JUNIOR", "初中"),
    SENIOR("SENIOR", "高中");

    private final String code;
    private final String desc;

    StageEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 校验学段是否合法
     */
    public static boolean isValid(String code) {
        if (code == null || code.isEmpty()) {
            return false;
        }
        for (StageEnum stage : values()) {
            if (stage.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}
