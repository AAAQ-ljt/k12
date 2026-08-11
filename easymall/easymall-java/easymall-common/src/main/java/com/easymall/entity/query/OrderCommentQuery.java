package com.easymall.entity.query;

/**
 * 参数
 */
public class OrderCommentQuery extends BaseParam {


    /**
     * 订单ID
     */
    private String orderId;

    private String orderIdFuzzy;

    /**
     * 商品ID
     */
    private String productId;

    private String productIdFuzzy;

    /**
     * 评价内容
     */
    private String commentContent;

    private String commentContentFuzzy;

    /**
     * 评价时间
     */
    private String commentTime;

    private String commentTimeStart;

    private String commentTimeEnd;

    /**
     * 评价图片
     */
    private String commentImages;

    private String commentImagesFuzzy;

    /**
     * 评价星级
     */
    private Integer star;

    /**
     * 商家回复
     */
    private String commentBizReply;

    private String commentBizReplyFuzzy;

    /**
     * 追评
     */
    private String recommentContent;

    private String recommentContentFuzzy;

    /**
     * 追评时间
     */
    private String recommentTime;

    private String recommentTimeStart;

    private String recommentTimeEnd;

    /**
     * 追评图片
     */
    private String recommentImages;

    private String recommentImagesFuzzy;

    /**
     * 用户ID
     */
    private String userId;

    private String userIdFuzzy;

    /**
     * 属性信息
     */
    private String propertyInfo;

    /**
     * 0:正常 1:已删除
     */
    private Integer status;

    private String propertyInfoFuzzy;

    private Boolean queryUserInfo;

    private Boolean queryProduct;

    private String nickNameFuzzy;

    public void setNickNameFuzzy(String nickNameFuzzy) {
        this.nickNameFuzzy = nickNameFuzzy;
    }

    public void setProductNameFuzzy(String productNameFuzzy) {
        this.productNameFuzzy = productNameFuzzy;
    }

    private String productNameFuzzy;

    public String getNickNameFuzzy() {
        return nickNameFuzzy;
    }

    public String getProductNameFuzzy() {
        return productNameFuzzy;
    }

    public Boolean getQueryUserInfo() {
        return queryUserInfo;
    }

    public void setQueryUserInfo(Boolean queryUserInfo) {
        this.queryUserInfo = queryUserInfo;
    }

    public Boolean getQueryProduct() {
        return queryProduct;
    }

    public void setQueryProduct(Boolean queryProduct) {
        this.queryProduct = queryProduct;
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

    public void setCommentContent(String commentContent) {
        this.commentContent = commentContent;
    }

    public String getCommentContent() {
        return this.commentContent;
    }

    public void setCommentContentFuzzy(String commentContentFuzzy) {
        this.commentContentFuzzy = commentContentFuzzy;
    }

    public String getCommentContentFuzzy() {
        return this.commentContentFuzzy;
    }

    public void setCommentTime(String commentTime) {
        this.commentTime = commentTime;
    }

    public String getCommentTime() {
        return this.commentTime;
    }

    public void setCommentTimeStart(String commentTimeStart) {
        this.commentTimeStart = commentTimeStart;
    }

    public String getCommentTimeStart() {
        return this.commentTimeStart;
    }

    public void setCommentTimeEnd(String commentTimeEnd) {
        this.commentTimeEnd = commentTimeEnd;
    }

    public String getCommentTimeEnd() {
        return this.commentTimeEnd;
    }

    public void setCommentImages(String commentImages) {
        this.commentImages = commentImages;
    }

    public String getCommentImages() {
        return this.commentImages;
    }

    public void setCommentImagesFuzzy(String commentImagesFuzzy) {
        this.commentImagesFuzzy = commentImagesFuzzy;
    }

    public String getCommentImagesFuzzy() {
        return this.commentImagesFuzzy;
    }

    public void setStar(Integer star) {
        this.star = star;
    }

    public Integer getStar() {
        return this.star;
    }

    public void setCommentBizReply(String commentBizReply) {
        this.commentBizReply = commentBizReply;
    }

    public String getCommentBizReply() {
        return this.commentBizReply;
    }

    public void setCommentBizReplyFuzzy(String commentBizReplyFuzzy) {
        this.commentBizReplyFuzzy = commentBizReplyFuzzy;
    }

    public String getCommentBizReplyFuzzy() {
        return this.commentBizReplyFuzzy;
    }

    public void setRecommentContent(String recommentContent) {
        this.recommentContent = recommentContent;
    }

    public String getRecommentContent() {
        return this.recommentContent;
    }

    public void setRecommentContentFuzzy(String recommentContentFuzzy) {
        this.recommentContentFuzzy = recommentContentFuzzy;
    }

    public String getRecommentContentFuzzy() {
        return this.recommentContentFuzzy;
    }

    public void setRecommentTime(String recommentTime) {
        this.recommentTime = recommentTime;
    }

    public String getRecommentTime() {
        return this.recommentTime;
    }

    public void setRecommentTimeStart(String recommentTimeStart) {
        this.recommentTimeStart = recommentTimeStart;
    }

    public String getRecommentTimeStart() {
        return this.recommentTimeStart;
    }

    public void setRecommentTimeEnd(String recommentTimeEnd) {
        this.recommentTimeEnd = recommentTimeEnd;
    }

    public String getRecommentTimeEnd() {
        return this.recommentTimeEnd;
    }

    public void setRecommentImages(String recommentImages) {
        this.recommentImages = recommentImages;
    }

    public String getRecommentImages() {
        return this.recommentImages;
    }

    public void setRecommentImagesFuzzy(String recommentImagesFuzzy) {
        this.recommentImagesFuzzy = recommentImagesFuzzy;
    }

    public String getRecommentImagesFuzzy() {
        return this.recommentImagesFuzzy;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserIdFuzzy(String userIdFuzzy) {
        this.userIdFuzzy = userIdFuzzy;
    }

    public String getUserIdFuzzy() {
        return this.userIdFuzzy;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
