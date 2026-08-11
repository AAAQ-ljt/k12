package com.easymall.entity.query;


import java.util.List;

/**
 * 参数
 */
public class ProductPropertyValueQuery extends BaseParam {


    /**
     * 商品ID
     */
    private String productId;

    private String productIdFuzzy;

    /**
     * 属性ID
     */
    private String propertyId;

    private String propertyIdFuzzy;

    /**
     * 属性名称
     */
    private String propertyName;

    private String propertyNameFuzzy;

    /**
     * 属性排序
     */
    private Integer propertySort;

    /**
     * 0:无需传封面 1:需传封面
     */
    private Integer coverType;

    /**
     *
     */
    private String propertyValueId;

    private String propertyValueIdFuzzy;

    /**
     * 属性封面
     */
    private String propertyCover;

    private String propertyCoverFuzzy;

    /**
     * 属性值
     */
    private String propertyValue;

    private String propertyValueFuzzy;

    /**
     * 备注
     */
    private String propertyRemark;

    private String propertyRemarkFuzzy;

    /**
     * 属性值排序
     */
    private Integer sort;


    private List<String> productIdList;

    public List<String> getProductIdList() {
        return productIdList;
    }

    public void setProductIdList(List<String> productIdList) {
        this.productIdList = productIdList;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductId() {
        return this.productId;
    }

    public void setProductIdFuzzy(String productIdFuzzy) {
        this.productIdFuzzy = productIdFuzzy;
    }

    public String getProductIdFuzzy() {
        return this.productIdFuzzy;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    public String getPropertyId() {
        return this.propertyId;
    }

    public void setPropertyIdFuzzy(String propertyIdFuzzy) {
        this.propertyIdFuzzy = propertyIdFuzzy;
    }

    public String getPropertyIdFuzzy() {
        return this.propertyIdFuzzy;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyName() {
        return this.propertyName;
    }

    public void setPropertyNameFuzzy(String propertyNameFuzzy) {
        this.propertyNameFuzzy = propertyNameFuzzy;
    }

    public String getPropertyNameFuzzy() {
        return this.propertyNameFuzzy;
    }

    public void setPropertySort(Integer propertySort) {
        this.propertySort = propertySort;
    }

    public Integer getPropertySort() {
        return this.propertySort;
    }

    public void setCoverType(Integer coverType) {
        this.coverType = coverType;
    }

    public Integer getCoverType() {
        return this.coverType;
    }

    public void setPropertyValueId(String propertyValueId) {
        this.propertyValueId = propertyValueId;
    }

    public String getPropertyValueId() {
        return this.propertyValueId;
    }

    public void setPropertyValueIdFuzzy(String propertyValueIdFuzzy) {
        this.propertyValueIdFuzzy = propertyValueIdFuzzy;
    }

    public String getPropertyValueIdFuzzy() {
        return this.propertyValueIdFuzzy;
    }

    public void setPropertyCover(String propertyCover) {
        this.propertyCover = propertyCover;
    }

    public String getPropertyCover() {
        return this.propertyCover;
    }

    public void setPropertyCoverFuzzy(String propertyCoverFuzzy) {
        this.propertyCoverFuzzy = propertyCoverFuzzy;
    }

    public String getPropertyCoverFuzzy() {
        return this.propertyCoverFuzzy;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public String getPropertyValue() {
        return this.propertyValue;
    }

    public void setPropertyValueFuzzy(String propertyValueFuzzy) {
        this.propertyValueFuzzy = propertyValueFuzzy;
    }

    public String getPropertyValueFuzzy() {
        return this.propertyValueFuzzy;
    }

    public void setPropertyRemark(String propertyRemark) {
        this.propertyRemark = propertyRemark;
    }

    public String getPropertyRemark() {
        return this.propertyRemark;
    }

    public void setPropertyRemarkFuzzy(String propertyRemarkFuzzy) {
        this.propertyRemarkFuzzy = propertyRemarkFuzzy;
    }

    public String getPropertyRemarkFuzzy() {
        return this.propertyRemarkFuzzy;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getSort() {
        return this.sort;
    }

}
