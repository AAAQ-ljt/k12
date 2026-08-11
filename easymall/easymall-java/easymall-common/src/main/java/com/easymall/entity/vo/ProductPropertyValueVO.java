package com.easymall.entity.vo;

public class ProductPropertyValueVO {
    private String propertyValueId;
    private String propertyCover;
    private String propertyValue;
    private String propertyRemark;

    public ProductPropertyValueVO() {
    }

    public ProductPropertyValueVO(String propertyValueId, String propertyCover, String propertyValue, String propertyRemark) {
        this.propertyValueId = propertyValueId;
        this.propertyCover = propertyCover;
        this.propertyValue = propertyValue;
        this.propertyRemark = propertyRemark;
    }

    public String getPropertyCover() {
        return propertyCover;
    }

    public void setPropertyCover(String propertyCover) {
        this.propertyCover = propertyCover;
    }

    public String getPropertyValueId() {
        return propertyValueId;
    }

    public void setPropertyValueId(String propertyValueId) {
        this.propertyValueId = propertyValueId;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public String getPropertyRemark() {
        return propertyRemark;
    }

    public void setPropertyRemark(String propertyRemark) {
        this.propertyRemark = propertyRemark;
    }
}
