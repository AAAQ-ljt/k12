package com.easymall.entity.vo;

import com.easymall.entity.po.ProductInfo;
import com.easymall.entity.po.ProductSku;

import java.util.List;

public class ProductInfoDetailVO {
    private ProductInfo productInfo;
    private List<ProductPropertyVO> productPropertyList;
    private List<ProductSku> skuList;

    public ProductInfo getProductInfo() {
        return productInfo;
    }

    public void setProductInfo(ProductInfo productInfo) {
        this.productInfo = productInfo;
    }

    public List<ProductPropertyVO> getProductPropertyList() {
        return productPropertyList;
    }

    public void setProductPropertyList(List<ProductPropertyVO> productPropertyList) {
        this.productPropertyList = productPropertyList;
    }

    public List<ProductSku> getSkuList() {
        return skuList;
    }

    public void setSkuList(List<ProductSku> skuList) {
        this.skuList = skuList;
    }
}
