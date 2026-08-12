package com.easymall.entity.enums;

public enum MessageOutPutTypeEnum {
    OUTPUTTING(0, "输出中"), DONE(1, "处理完成"), ERROR(2, "输出失败");
    private Integer type;
    private String desc;

    MessageOutPutTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
