package com.easymall.entity.dto;

import com.easymall.entity.enums.OrderStatusEnum;

public class OrderStatusDTO {

    private Integer status;

    private String desc;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static OrderStatusDTO from(OrderStatusEnum status) {
        OrderStatusDTO statusDTO = new OrderStatusDTO();
        statusDTO.setStatus(status.getStatus());
        statusDTO.setDesc(status.getDesc());
        return statusDTO;
    }
}
