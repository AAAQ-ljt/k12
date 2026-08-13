package com.nexora.entity.enums;

/**
 * 教学域用户意图
 */
public enum UserIntentEnum {

    EXPLAIN("EXPLAIN", "讲解"),
    RECOMMEND("RECOMMEND", "推荐"),
    QUIZ("QUIZ", "练习"),
    PICTURE_BOOK("PICTURE_BOOK", "绘本"),
    DRAW("DRAW", "画图"),
    ANIMATION("ANIMATION", "动画"),
    CODING("CODING", "编程"),
    PLAN("PLAN", "学习路径"),
    PROGRESS("PROGRESS", "进度掌握度"),
    CHAT("CHAT", "普通对话");

    private final String code;
    private final String desc;

    UserIntentEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static boolean isValid(String code) {
        if (code == null) {
            return false;
        }
        for (UserIntentEnum item : values()) {
            if (item.code.equals(code)) {
                return true;
            }
        }
        return false;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
