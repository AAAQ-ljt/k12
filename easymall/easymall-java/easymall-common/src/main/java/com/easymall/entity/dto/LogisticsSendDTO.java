package com.easymall.entity.dto;

public class LogisticsSendDTO {

    /**
     * 发货人姓名
     */
    private String senderName;

    /**
     * 发货人电话
     */
    private String senderPhone;

    /**
     * 发货地址
     */
    private String senderAddress;

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderPhone() {
        return senderPhone;
    }

    public void setSenderPhone(String senderPhone) {
        this.senderPhone = senderPhone;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }
}
