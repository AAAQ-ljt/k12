package com.smart.campus.entity.query;

public class MessageInfoQuery extends BaseParam {

    private Long messageId;
    private String messageTitle;
    private String messageTitleFuzzy;
    private String messageContent;
    private String messageContentFuzzy;
    private Integer messageType;
    private Integer bizType;
    private String bizId;
    private String bizIdFuzzy;
    private Integer senderId;
    private String senderName;
    private String senderNameFuzzy;
    private Integer sendScope;
    private String jumpPath;
    private String jumpPathFuzzy;
    private String sendTime;
    private String sendTimeStart;
    private String sendTimeEnd;
    private String createTime;
    private String createTimeStart;
    private String createTimeEnd;

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getMessageTitle() {
        return messageTitle;
    }

    public void setMessageTitle(String messageTitle) {
        this.messageTitle = messageTitle;
    }

    public String getMessageTitleFuzzy() {
        return messageTitleFuzzy;
    }

    public void setMessageTitleFuzzy(String messageTitleFuzzy) {
        this.messageTitleFuzzy = messageTitleFuzzy;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public String getMessageContentFuzzy() {
        return messageContentFuzzy;
    }

    public void setMessageContentFuzzy(String messageContentFuzzy) {
        this.messageContentFuzzy = messageContentFuzzy;
    }

    public Integer getMessageType() {
        return messageType;
    }

    public void setMessageType(Integer messageType) {
        this.messageType = messageType;
    }

    public Integer getBizType() {
        return bizType;
    }

    public void setBizType(Integer bizType) {
        this.bizType = bizType;
    }

    public String getBizId() {
        return bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public String getBizIdFuzzy() {
        return bizIdFuzzy;
    }

    public void setBizIdFuzzy(String bizIdFuzzy) {
        this.bizIdFuzzy = bizIdFuzzy;
    }

    public Integer getSenderId() {
        return senderId;
    }

    public void setSenderId(Integer senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderNameFuzzy() {
        return senderNameFuzzy;
    }

    public void setSenderNameFuzzy(String senderNameFuzzy) {
        this.senderNameFuzzy = senderNameFuzzy;
    }

    public Integer getSendScope() {
        return sendScope;
    }

    public void setSendScope(Integer sendScope) {
        this.sendScope = sendScope;
    }

    public String getJumpPath() {
        return jumpPath;
    }

    public void setJumpPath(String jumpPath) {
        this.jumpPath = jumpPath;
    }

    public String getJumpPathFuzzy() {
        return jumpPathFuzzy;
    }

    public void setJumpPathFuzzy(String jumpPathFuzzy) {
        this.jumpPathFuzzy = jumpPathFuzzy;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public String getSendTimeStart() {
        return sendTimeStart;
    }

    public void setSendTimeStart(String sendTimeStart) {
        this.sendTimeStart = sendTimeStart;
    }

    public String getSendTimeEnd() {
        return sendTimeEnd;
    }

    public void setSendTimeEnd(String sendTimeEnd) {
        this.sendTimeEnd = sendTimeEnd;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getCreateTimeStart() {
        return createTimeStart;
    }

    public void setCreateTimeStart(String createTimeStart) {
        this.createTimeStart = createTimeStart;
    }

    public String getCreateTimeEnd() {
        return createTimeEnd;
    }

    public void setCreateTimeEnd(String createTimeEnd) {
        this.createTimeEnd = createTimeEnd;
    }
}
