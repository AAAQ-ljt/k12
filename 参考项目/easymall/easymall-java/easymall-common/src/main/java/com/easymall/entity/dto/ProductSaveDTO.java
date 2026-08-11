package com.easymall.entity.dto;

import com.easymall.entity.po.ProductInfo;
import com.easymall.entity.po.ProductPropertyValue;
import com.easymall.entity.po.ProductSku;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

public class ProductSaveDTO {

    @Valid
    private ProductInfo productInfo;

    @Valid
    @Size(min = 1)
    private List<ProductPropertyValue> productPropertyList;

    @Valid
    @Size(min = 1)
    private List<ProductSku> skuList;

    public ProductInfo getProductInfo() {
        return productInfo;
    }

    public void setProductInfo(ProductInfo productInfo) {
        this.productInfo = productInfo;
    }

    public List<ProductPropertyValue> getProductPropertyList() {
        return productPropertyList;
    }

    public void setProductPropertyList(List<ProductPropertyValue> productPropertyList) {
        this.productPropertyList = productPropertyList;
    }

    public List<ProductSku> getSkuList() {
        return skuList;
    }

    public void setSkuList(List<ProductSku> skuList) {
        this.skuList = skuList;
    }
}
