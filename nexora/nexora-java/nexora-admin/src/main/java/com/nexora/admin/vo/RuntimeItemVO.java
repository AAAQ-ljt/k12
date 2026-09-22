package com.nexora.admin.vo;

/**
 * 运行时环境/模型信息项（只读展示，敏感值一律脱敏）
 */
public class RuntimeItemVO {

    /** 展示名称 */
    private String label;

    /** 展示值（Key 类已脱敏） */
    private String value;

    /** 补充说明（如"改动需重启服务"） */
    private String remark;

    public RuntimeItemVO() {
    }

    public RuntimeItemVO(String label, String value, String remark) {
        this.label = label;
        this.value = value;
        this.remark = remark;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
