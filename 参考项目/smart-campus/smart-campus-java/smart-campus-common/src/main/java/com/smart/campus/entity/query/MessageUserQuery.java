package com.smart.campus.entity.query;

public class MessageUserQuery extends BaseParam {

    private Long id;
    private Long messageId;
    private Integer userId;
    private Integer readFlag;
    private String readTime;
    private String readTimeStart;
    private String readTimeEnd;
    private Integer deleteFlag;
    private String createTime;
    private String createTimeStart;
    private String createTimeEnd;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getReadFlag() {
        return readFlag;
    }

    public void setReadFlag(Integer readFlag) {
        this.readFlag = readFlag;
    }

    public String getReadTime() {
        return readTime;
    }

    public void setReadTime(String readTime) {
        this.readTime = readTime;
    }

    public String getReadTimeStart() {
        return readTimeStart;
    }

    public void setReadTimeStart(String readTimeStart) {
        this.readTimeStart = readTimeStart;
    }

    public String getReadTimeEnd() {
        return readTimeEnd;
    }

    public void setReadTimeEnd(String readTimeEnd) {
        this.readTimeEnd = readTimeEnd;
    }

    public Integer getDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(Integer deleteFlag) {
        this.deleteFlag = deleteFlag;
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
