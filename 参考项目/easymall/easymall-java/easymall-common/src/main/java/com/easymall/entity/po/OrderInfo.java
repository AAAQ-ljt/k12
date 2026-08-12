package com.easymall.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.easymall.entity.enums.DateTimePatternEnum;
import com.easymall.entity.enums.OrderStatusEnum;
import com.easymall.utils.DateUtil;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


/**
 * 订单信息
 */
public class OrderInfo implements Serializable {


    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 订单创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date orderTime;

    /**
     * -1已删除 0:待付款 1:已付款,待发货  2:已发货  3:已完成 4:已取消 5:已关闭 6:已退款 7:部分退款
     */
    private Integer orderStatus;

    /**
     * 支付通道
     */
    private String payChannel;

    /**
     * 支付场景
     */
    private String payScene;

    /**
     * 支付订单号
     */
    private String payOrderId;

    /**
     * 通道ID
     */
    private String channelOrderId;

    /**
     * 评价状态 0:未评价  1:已评价  2:已追评
     */
    private Integer commentStatus;


    private List<OrderItem> orderItemList;

    private String orderStatusName;

    private String nickName;

    private String avatar;

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getOrderStatusName() {
        OrderStatusEnum orderStatusEnum = OrderStatusEnum.getByStatus(orderStatus);
        return orderStatusEnum == null ? null : orderStatusEnum.getDesc();
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return this.orderId;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setOrderTime(Date orderTime) {
        this.orderTime = orderTime;
    }

    public Date getOrderTime() {
        return this.orderTime;
    }

    public void setOrderStatus(Integer orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Integer getOrderStatus() {
        return this.orderStatus;
    }

    public void setPayChannel(String payChannel) {
        this.payChannel = payChannel;
    }

    public String getPayChannel() {
        return this.payChannel;
    }

    public void setPayScene(String payScene) {
        this.payScene = payScene;
    }

    public String getPayScene() {
        return this.payScene;
    }

    public void setPayOrderId(String payOrderId) {
        this.payOrderId = payOrderId;
    }

    public String getPayOrderId() {
        return this.payOrderId;
    }

    public void setChannelOrderId(String channelOrderId) {
        this.channelOrderId = channelOrderId;
    }

    public String getChannelOrderId() {
        return this.channelOrderId;
    }

    public void setCommentStatus(Integer commentStatus) {
        this.commentStatus = commentStatus;
    }

    public Integer getCommentStatus() {
        return this.commentStatus;
    }

    public List<OrderItem> getOrderItemList() {
        return orderItemList;
    }

    public void setOrderItemList(List<OrderItem> orderItemList) {
        this.orderItemList = orderItemList;
    }

    public void setOrderStatusName(String orderStatusName) {
        this.orderStatusName = orderStatusName;
    }

    @Override
    public String toString() {
        return "订单ID:" + (orderId == null ? "空" : orderId) + "，金额:" + (amount == null ? "空" : amount) + "，用户ID:" + (userId == null ? "空" : userId) + "，订单创建时间:" + (orderTime == null ? "空" : DateUtil.format(orderTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + "，-1已删除 0:待付款 1:已付款,待发货  2:已发货  3:已完成 4:已取消 5:已关闭 6:已退款 7:部分退款:" + (orderStatus == null ? "空" : orderStatus) + "，支付通道:" + (payChannel == null ? "空" : payChannel) + "，支付场景:" + (payScene == null ? "空" : payScene) + "，支付订单号:" + (payOrderId == null ? "空" : payOrderId) + "，通道ID:" + (channelOrderId == null ? "空" : channelOrderId) + "，评价状态 0:未评价  1:已评价  2:已追评:" + (commentStatus == null ? "空" : commentStatus);
    }
}
