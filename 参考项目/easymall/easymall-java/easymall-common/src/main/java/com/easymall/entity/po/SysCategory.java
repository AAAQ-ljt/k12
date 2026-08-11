package com.easymall.entity.po;

import java.io.Serializable;
import java.util.List;


/**
 *
 */
public class SysCategory implements Serializable {


    /**
     * 分类ID
     */
    private String categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 父ID
     */
    private String pCategoryId;

    /**
     * 排序号
     */
    private Integer sort;

    private List<SysCategory> children;

    private List<SysProductProperty> productPropertyList;

    public List<SysProductProperty> getProductPropertyList() {
        return productPropertyList;
    }

    public void setProductPropertyList(List<SysProductProperty> productPropertyList) {
        this.productPropertyList = productPropertyList;
    }

    public List<SysCategory> getChildren() {
        return children;
    }

    public void setChildren(List<SysCategory> children) {
        this.children = children;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryId() {
        return this.categoryId;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryName() {
        return this.categoryName;
    }

    public void setpCategoryId(String pCategoryId) {
        this.pCategoryId = pCategoryId;
    }

    public String getpCategoryId() {
        return this.pCategoryId;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getSort() {
        return this.sort;
    }

    @Override
    public String toString() {
        return "分类ID:" + (categoryId == null ? "空" : categoryId) + "，分类名称:" + (categoryName == null ? "空" : categoryName) + "，父ID:" + (pCategoryId == null ? "空" :
                pCategoryId) + "，排序号:" + (sort == null ? "空" : sort);
    }
}
