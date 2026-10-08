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

    /**
     * 任务级自动重试次数（2026-10-08）：生文网关会间歇性整轮超时（实测线上 150s 读超时被掐断），
     * 超时不该把任务直接判死——重试到上限才失败，期间学生在进度卡上看到的是「自动重试中」。
     */
    /** 来源对话消息ID（对话内发起时带上；任务完成时把结果回写进这条消息，
     * 之后历史对话直接渲染动画，不再依赖 2 小时就回收的 Redis 任务体）。动画讲解页发起时为空。 */
    private String messageId;

    private Integer retryCount;

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

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}