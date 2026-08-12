package com.easymall.entity.vo;

import java.math.BigDecimal;
import java.util.List;

public class ProductSkuVO {
    private String cartId;
    private String productId;
    private String productName;
    private String productCover;
    private String propertyValueIds;
    private String propertyValueIdHash;
    private List<ProductSkuProperDataVO> propertyData;
    private BigDecimal price;
    private Integer stock;
    private Integer buyCount;
    private Boolean productOnSale;

    public Boolean getProductOnSale() {
        return productOnSale;
    }

    public void setProductOnSale(Boolean productOnSale) {
        this.productOnSale = productOnSale;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductCover() {
        return productCover;
    }

    public void setProductCover(String productCover) {
        this.productCover = productCover;
    }

    public String getPropertyValueIds() {
        return propertyValueIds;
    }

    public void setPropertyValueIds(String propertyValueIds) {
        this.propertyValueIds = propertyValueIds;
    }

    public String getPropertyValueIdHash() {
        return propertyValueIdHash;
    }

    public void setPropertyValueIdHash(String propertyValueIdHash) {
        this.propertyValueIdHash = propertyValueIdHash;
    }

    public List<ProductSkuProperDataVO> getPropertyData() {
        return propertyData;
    }

    public void setPropertyData(List<ProductSkuProperDataVO> propertyData) {
        this.propertyData = propertyData;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getBuyCount() {
        return buyCount;
    }

    public void setBuyCount(Integer buyCount) {
        this.buyCount = buyCount;
    }

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }
}
