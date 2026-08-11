package com.easymall.entity.po;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 *
 */
public class ProductSku implements Serializable {


    /**
     * 商品ID
     */
    private String productId;

    /**
     * 属性值id组
     */
    @NotEmpty
    private String propertyValueIdHash;


    @NotEmpty
    private String propertyValueIds;

    /**
     * 价格
     */
    @NotEmpty
    private BigDecimal price;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 排序
     */
    @NotNull
    private Integer sort;

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductId() {
        return this.productId;
    }

    public String getPropertyValueIdHash() {
        return propertyValueIdHash;
    }

    public void setPropertyValueIdHash(String propertyValueIdHash) {
        this.propertyValueIdHash = propertyValueIdHash;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getPrice() {
        return this.price;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getStock() {
        return this.stock;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getSort() {
        return this.sort;
    }

    public String getPropertyValueIds() {
        return propertyValueIds;
    }

    public void setPropertyValueIds(String propertyValueIds) {
        this.propertyValueIds = propertyValueIds;
    }
}
