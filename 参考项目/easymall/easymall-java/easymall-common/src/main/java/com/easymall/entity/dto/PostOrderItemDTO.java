package com.easymall.entity.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class PostOrderItemDTO {

    @NotEmpty
    private String productId;

    @NotEmpty
    private String propertyValueIds;

    @NotNull
    private Integer buyCount;

    private String remark;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getPropertyValueIds() {
        return propertyValueIds;
    }

    public void setPropertyValueIds(String propertyValueIds) {
        this.propertyValueIds = propertyValueIds;
    }

    public Integer getBuyCount() {
        return buyCount;
    }

    public void setBuyCount(Integer buyCount) {
        this.buyCount = buyCount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
