package com.easymall.entity.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class PostOrderDTO {

    @NotEmpty
    private String payMethod;

    @NotEmpty
    private String addressId;

    @Valid
    private List<PostOrderItemDTO> orderList;

    @NotNull
    private Integer orderFrom;

    public Integer getOrderFrom() {
        return orderFrom;
    }

    public void setOrderFrom(Integer orderFrom) {
        this.orderFrom = orderFrom;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public List<PostOrderItemDTO> getOrderList() {
        return orderList;
    }

    public void setOrderList(List<PostOrderItemDTO> orderList) {
        this.orderList = orderList;
    }

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }
}
