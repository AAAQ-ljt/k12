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

    /**
     * 根据年级匹配学段（K12 惯例，映射规则如需调整只改这里）
     * 一年级/二年级 -> 小学低；三年级~六年级 -> 小学高；初一~初三 -> 初中；高一~高三 -> 高中
     */
    public static String matchByGrade(String grade) {
        if (grade == null || grade.isEmpty()) {
            return null;
        }
        return switch (grade) {
            case "一年级", "二年级" -> PRIMARY_LOW.code;
            case "三年级", "四年级", "五年级", "六年级" -> PRIMARY_HIGH.code;
            case "初一", "初二", "初三" -> JUNIOR.code;
            case "高一", "高二", "高三" -> SENIOR.code;
            default -> null;
        };
    }
}
