package com.nexora.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 动画讲解生成异步任务体/状态（Redis 持久化，前端轮询）
 */
public class AnimationTaskVO {

    /** 任务ID */
    private String taskId;

    /** 学生ID */
    private String userId;

    /** 学段 */
    private String stage;

    /** 动画主题/知识点 */
    private String topic;

    /**
     * 状态机：PENDING → ANIMATION_GENERATING → COMPLETED / FAILED
     */
    private String status;

    /** 任务说明/错误信息 */
    private String message;

    /** 动画标题 */
    private String title;

    /** 完成后的动画资源ID（个人知识库 ANIMATION 资源） */
    private String animationResourceId;

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

    public String getAnimationResourceId() {
        return animationResourceId;
    }

    public void setAnimationResourceId(String animationResourceId) {
        this.animationResourceId = animationResourceId;
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