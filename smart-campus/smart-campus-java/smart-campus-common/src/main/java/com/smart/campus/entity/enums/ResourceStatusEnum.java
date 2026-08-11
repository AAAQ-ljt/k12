package com.smart.campus.entity.enums;

public enum ResourceStatusEnum {

    UPLOADING(1, "上传中"),
    TRANSCODING(2, "转码中"),
    SUCCESS(3, "上传成功"),
    TRANSCODE_FAILED(4, "转码失败"),
    UPLOAD_FAILED(5, "上传失败");

    private final Integer code;

    private final String desc;

    ResourceStatusEnum(Integer code, String desc) {
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
