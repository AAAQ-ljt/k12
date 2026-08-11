package com.easymall.entity.po;

import com.easymall.entity.enums.LogisticsStatusEnum;

import java.io.Serializable;
import java.util.List;


/**
 * 物流信息表
 */
public class OrderLogisticsInfo implements Serializable {


    /**
     * 订单编号
     */
    private String orderId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 物流单号
     */
    private String logisticsNo;

    /**
     * 物流公司
     */
    private String logisticsCompany;

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

    /**
     * 收件人姓名
     */
    private String receiverName;

    /**
     * 收件人电话
     */
    private String receiverPhone;

    /**
     * 收件地址
     */
    private String receiverAddress;

    /**
     * 物流状态：0待发货 1运输中 2已送达 3订单取消
     */
    private Integer logisticsStatus;

    private String logisticsStatusName;

    public String getLogisticsStatusName() {
        LogisticsStatusEnum logisticsStatus = LogisticsStatusEnum.getByStatus(this.logisticsStatus);
        return logisticsStatus == null ? null : logisticsStatus.getDesc();
    }

    private List<OrderLogisticsInfoRecord> recordList;

    public List<OrderLogisticsInfoRecord> getRecordList() {
        return recordList;
    }

    public void setRecordList(List<OrderLogisticsInfoRecord> recordList) {
        this.recordList = recordList;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return this.orderId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setLogisticsNo(String logisticsNo) {
        this.logisticsNo = logisticsNo;
    }

    public String getLogisticsNo() {
        return this.logisticsNo;
    }

    public void setLogisticsCompany(String logisticsCompany) {
        this.logisticsCompany = logisticsCompany;
    }

    public String getLogisticsCompany() {
        return this.logisticsCompany;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderName() {
        return this.senderName;
    }

    public void setSenderPhone(String senderPhone) {
        this.senderPhone = senderPhone;
    }

    public String getSenderPhone() {
        return this.senderPhone;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public String getSenderAddress() {
        return this.senderAddress;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverName() {
        return this.receiverName;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public String getReceiverPhone() {
        return this.receiverPhone;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public String getReceiverAddress() {
        return this.receiverAddress;
    }

    public void setLogisticsStatus(Integer logisticsStatus) {
        this.logisticsStatus = logisticsStatus;
    }

    public Integer getLogisticsStatus() {
        return this.logisticsStatus;
    }

    @Override
    public String toString() {
        return "订单编号:" + (orderId == null ? "空" : orderId) + "，用户ID:" + (userId == null ? "空" : userId) + "，物流单号:" + (logisticsNo == null ? "空" : logisticsNo) +
                "，物流公司:" + (logisticsCompany == null ? "空" : logisticsCompany) + "，发货人姓名:" + (senderName == null ? "空" : senderName) + "，发货人电话:" + (senderPhone == null ? "空" : senderPhone) + "，发货地址:" + (senderAddress == null ? "空" : senderAddress) + "，收件人姓名:" + (receiverName == null ? "空" : receiverName) + "，收件人电话:" + (receiverPhone == null ? "空" : receiverPhone) + "，收件地址:" + (receiverAddress == null ? "空" : receiverAddress) + "，物流状态：0待发货 1运输中 2已送达 3订单取消:" + (logisticsStatus == null ? "空" : logisticsStatus);
    }
}
