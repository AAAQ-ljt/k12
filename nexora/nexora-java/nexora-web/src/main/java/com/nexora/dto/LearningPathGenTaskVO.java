package com.nexora.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 学习路径 AI 生成异步任务体/状态（Redis 持久化，前端轮询）。
 *
 * 状态机：PENDING → PATH_GENERATING → COMPLETED / FAILED。
 * 生成完成时 pathId 承载新路线ID，前端轮询到终态后跳转详情；任务期间「AI 生成新路线」按钮禁用，
 * 后端按用户互斥，防止连续点击重复生成多条路线。
 */
public class LearningPathGenTaskVO {

    /** 任务ID */
    private String taskId;

    /** 学生ID */
    private String userId;

    /** 学段 */
    private String stage;

    /** 状态机：PENDING → PATH_GENERATING → COMPLETED / FAILED */
    private String status;

    /** 任务说明/错误信息 */
    private String message;

    /** 完成后的路线标题 */
    private String title;

    /** 完成后的路线ID（前端跳转详情） */
    private String pathId;

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

    public String getPathId() {
        return pathId;
    }

    public void setPathId(String pathId) {
        this.pathId = pathId;
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