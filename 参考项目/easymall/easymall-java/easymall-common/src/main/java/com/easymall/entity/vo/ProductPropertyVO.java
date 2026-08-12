package com.easymall.entity.vo;

import java.util.List;

public class ProductPropertyVO {
    private String propertyId;
    private String propertyName;
    private Integer propertySort;
    private Integer coverType;

    private List<ProductPropertyValueVO> propertyValues;

    public ProductPropertyVO() {
    }

    public ProductPropertyVO(String propertyId, String propertyName, Integer propertySort, Integer coverType) {
        this.propertyId = propertyId;
        this.propertyName = propertyName;
        this.propertySort = propertySort;
        this.coverType = coverType;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public List<ProductPropertyValueVO> getPropertyValues() {
        return propertyValues;
    }

    public void setPropertyValues(List<ProductPropertyValueVO> propertyValues) {
        this.propertyValues = propertyValues;
    }

    public Integer getPropertySort() {
        return propertySort;
    }

    public void setPropertySort(Integer propertySort) {
        this.propertySort = propertySort;
    }

    public Integer getCoverType() {
        return coverType;
    }

    public void setCoverType(Integer coverType) {
        this.coverType = coverType;
    }
}
