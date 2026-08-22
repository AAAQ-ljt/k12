package com.nexora.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 绘本生成异步任务体/状态（Redis 持久化，前端轮询）
 */
public class PictureBookTaskVO {

    /** 任务ID */
    private String taskId;

    /** 学生ID */
    private String userId;

    /** 学段 */
    private String stage;

    /** 主题 */
    private String topic;

    /**
     * 状态机：PENDING → STORY_GENERATING → STORY_DONE → IMAGE_GENERATING →
     * COMPLETED / FAILED
     */
    private String status;

    /** 当前已完成页数 */
    private int current;

    /** 总页数 */
    private int total;

    /** 任务说明/错误信息 */
    private String message;

    /** 绘本标题 */
    private String title;

    /** 完成后的绘本资源ID */
    private String bookResourceId;

    /** 分页文本与结果（内部使用，前端忽略） */
    private String pages;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBookResourceId() {
        return bookResourceId;
    }

    public void setBookResourceId(String bookResourceId) {
        this.bookResourceId = bookResourceId;
    }

    public String getPages() {
        return pages;
    }

    public void setPages(String pages) {
        this.pages = pages;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}