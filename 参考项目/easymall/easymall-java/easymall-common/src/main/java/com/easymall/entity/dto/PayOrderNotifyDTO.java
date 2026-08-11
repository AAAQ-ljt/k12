package com.easymall.entity.dto;

public class PayOrderNotifyDTO {
    /**
     * 支付订单ID
     */
    private String payOrderId;
    /**
     * 支付订单号 通道订单号
     */
    private String channelOrderId;

    public PayOrderNotifyDTO() {
    }

    public PayOrderNotifyDTO(String payOrderId, String channelOrderId) {
        this.payOrderId = payOrderId;
        this.channelOrderId = channelOrderId;
    }

    public String getPayOrderId() {
        return payOrderId;
    }

    public void setPayOrderId(String payOrderId) {
        this.payOrderId = payOrderId;
    }

    public String getChannelOrderId() {
        return channelOrderId;
    }

    public void setChannelOrderId(String channelOrderId) {
        this.channelOrderId = channelOrderId;
    }
}
