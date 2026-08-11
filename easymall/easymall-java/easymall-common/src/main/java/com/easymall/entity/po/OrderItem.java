package com.easymall.entity.po;

import com.easymall.entity.enums.OrderItemStatusEnum;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * 订单明细表
 */
public class OrderItem implements Serializable {


    /**
     * 订单明细ID
     */
    private String orderItemId;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 封面
     */
    private String cover;

    /**
     * 商品ID
     */
    private String productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 属性值id组hash
     */
    private String propertyValueIdHash;

    /**
     * 属性信息
     */
    private String propertyInfo;

    /**
     * 价格
     */
    private BigDecimal itemAmount;

    /**
     * 数量
     */
    private Integer buyCount;

    /**
     * 状态 1:正常 0:已退款
     */
    private Integer orderItemStatus;

    /**
     * 备注
     */
    private String remark;

    /**
     * 退款订单号
     */
    private String refundOrderId;

    private String orderItemStatusName;

    public String getOrderItemStatusName() {
        OrderItemStatusEnum orderItemStatusEnum = OrderItemStatusEnum.getByStatus(orderItemStatus);
        return orderItemStatusEnum == null ? "" : orderItemStatusEnum.getDesc();
    }

    public void setOrderItemId(String orderItemId) {
        this.orderItemId = orderItemId;
    }

    public String getOrderItemId() {
        return this.orderItemId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return this.orderId;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getCover() {
        return this.cover;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductId() {
        return this.productId;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductName() {
        return this.productName;
    }

    public void setPropertyValueIdHash(String propertyValueIdHash) {
        this.propertyValueIdHash = propertyValueIdHash;
    }

    public String getPropertyValueIdHash() {
        return this.propertyValueIdHash;
    }

    public void setPropertyInfo(String propertyInfo) {
        this.propertyInfo = propertyInfo;
    }

    public String getPropertyInfo() {
        return this.propertyInfo;
    }

    public void setItemAmount(BigDecimal itemAmount) {
        this.itemAmount = itemAmount;
    }

    public BigDecimal getItemAmount() {
        return this.itemAmount;
    }

    public void setBuyCount(Integer buyCount) {
        this.buyCount = buyCount;
    }

    public Integer getBuyCount() {
        return this.buyCount;
    }

    public void setOrderItemStatus(Integer orderItemStatus) {
        this.orderItemStatus = orderItemStatus;
    }

    public Integer getOrderItemStatus() {
        return this.orderItemStatus;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getRemark() {
        return this.remark;
    }

    public void setRefundOrderId(String refundOrderId) {
        this.refundOrderId = refundOrderId;
    }

    public String getRefundOrderId() {
        return this.refundOrderId;
    }

    @Override
    public String toString() {
        return "订单明细ID:" + (orderItemId == null ? "空" : orderItemId) + "，订单ID:" + (orderId == null ? "空" : orderId) + "，封面:" + (cover == null ? "空" : cover) + "，商品ID:" + (productId == null ? "空" : productId) + "，商品名称:" + (productName == null ? "空" : productName) + "，属性值id组hash:" + (propertyValueIdHash == null ? "空" : propertyValueIdHash) + "，属性信息:" + (propertyInfo == null ? "空" : propertyInfo) + "，价格:" + (itemAmount == null ? "空" : itemAmount) + "，数量:" + (buyCount == null ? "空" : buyCount) + "，状态 1:正常 0:已退款:" + (orderItemStatus == null ? "空" : orderItemStatus) + "，备注:" + (remark == null ? "空" : remark) + "，退款订单号:" + (refundOrderId == null ? "空" : refundOrderId);
    }
}
