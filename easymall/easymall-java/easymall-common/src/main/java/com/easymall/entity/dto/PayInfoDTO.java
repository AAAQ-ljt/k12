package com.easymall.entity.dto;

import java.math.BigDecimal;

public class PayInfoDTO {
    private String payInfo;
    private String payOrderId;
    private BigDecimal amount;

    public PayInfoDTO() {
    }

    public PayInfoDTO(String payInfo, String payOrderId, BigDecimal amount) {
        this.payInfo = payInfo;
        this.payOrderId = payOrderId;
        this.amount = amount;
    }

    public String getPayInfo() {
        return payInfo;
    }

    public void setPayInfo(String payInfo) {
        this.payInfo = payInfo;
    }

    public String getPayOrderId() {
        return payOrderId;
    }

    public void setPayOrderId(String payOrderId) {
        this.payOrderId = payOrderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
