package com.easymall.entity.query;

import java.math.BigDecimal;
import java.util.List;


/**
 * 订单明细表参数
 */
public class OrderItemQuery extends BaseParam {


    /**
     * 订单明细ID
     */
    private String orderItemId;

    private String orderItemIdFuzzy;

    /**
     * 订单ID
     */
    private String orderId;

    private String orderIdFuzzy;

    /**
     * 封面
     */
    private String cover;

    private String coverFuzzy;

    /**
     * 商品ID
     */
    private String productId;

    private String productIdFuzzy;

    /**
     * 商品名称
     */
    private String productName;

    private String productNameFuzzy;

    /**
     * 属性值id组hash
     */
    private String propertyValueIdHash;

    private String propertyValueIdHashFuzzy;

    /**
     * 属性信息
     */
    private String propertyInfo;

    private String propertyInfoFuzzy;

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

    private String remarkFuzzy;

    /**
     * 退款订单号
     */
    private String refundOrderId;

    private String refundOrderIdFuzzy;

    private List<String> orderIdList;

    public List<String> getOrderIdList() {
        return orderIdList;
    }

    public void setOrderIdList(List<String> orderIdList) {
        this.orderIdList = orderIdList;
    }

    public void setOrderItemId(String orderItemId) {
        this.orderItemId = orderItemId;
    }

    public String getOrderItemId() {
        return this.orderItemId;
    }

    public void setOrderItemIdFuzzy(String orderItemIdFuzzy) {
        this.orderItemIdFuzzy = orderItemIdFuzzy;
    }

    public String getOrderItemIdFuzzy() {
        return this.orderItemIdFuzzy;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return this.orderId;
    }

    public void setOrderIdFuzzy(String orderIdFuzzy) {
        this.orderIdFuzzy = orderIdFuzzy;
    }

    public String getOrderIdFuzzy() {
        return this.orderIdFuzzy;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getCover() {
        return this.cover;
    }

    public void setCoverFuzzy(String coverFuzzy) {
        this.coverFuzzy = coverFuzzy;
    }

    public String getCoverFuzzy() {
        return this.coverFuzzy;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductId() {
        return this.productId;
    }

    public void setProductIdFuzzy(String productIdFuzzy) {
        this.productIdFuzzy = productIdFuzzy;
    }

    public String getProductIdFuzzy() {
        return this.productIdFuzzy;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductName() {
        return this.productName;
    }

    public void setProductNameFuzzy(String productNameFuzzy) {
        this.productNameFuzzy = productNameFuzzy;
    }

    public String getProductNameFuzzy() {
        return this.productNameFuzzy;
    }

    public void setPropertyValueIdHash(String propertyValueIdHash) {
        this.propertyValueIdHash = propertyValueIdHash;
    }

    public String getPropertyValueIdHash() {
        return this.propertyValueIdHash;
    }

    public void setPropertyValueIdHashFuzzy(String propertyValueIdHashFuzzy) {
        this.propertyValueIdHashFuzzy = propertyValueIdHashFuzzy;
    }

    public String getPropertyValueIdHashFuzzy() {
        return this.propertyValueIdHashFuzzy;
    }

    public void setPropertyInfo(String propertyInfo) {
        this.propertyInfo = propertyInfo;
    }

    public String getPropertyInfo() {
        return this.propertyInfo;
    }

    public void setPropertyInfoFuzzy(String propertyInfoFuzzy) {
        this.propertyInfoFuzzy = propertyInfoFuzzy;
    }

    public String getPropertyInfoFuzzy() {
        return this.propertyInfoFuzzy;
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

    public void setRemarkFuzzy(String remarkFuzzy) {
        this.remarkFuzzy = remarkFuzzy;
    }

    public String getRemarkFuzzy() {
        return this.remarkFuzzy;
    }

    public void setRefundOrderId(String refundOrderId) {
        this.refundOrderId = refundOrderId;
    }

    public String getRefundOrderId() {
        return this.refundOrderId;
    }

    public void setRefundOrderIdFuzzy(String refundOrderIdFuzzy) {
        this.refundOrderIdFuzzy = refundOrderIdFuzzy;
    }

    public String getRefundOrderIdFuzzy() {
        return this.refundOrderIdFuzzy;
    }

}
