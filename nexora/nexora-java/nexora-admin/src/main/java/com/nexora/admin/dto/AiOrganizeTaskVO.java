package com.nexora.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * AI 文档整理异步任务体/状态（Redis 持久化，前端轮询）。
 *
 * 状态机：PENDING → ORGANIZING → COMPLETED / FAILED。
 * 完成后 organizedMd / originalText 承载整理稿与对照原文（编辑稿由前端本地维护并随 sessionStorage 保存，
 * 切页回到本页自动恢复，不再因页面切换丢失状态）；同一 resourceId 运行中互斥，重复提交返回进行中任务。
 */
public class AiOrganizeTaskVO {

    /** 任务ID */
    private String taskId;

    /** 源资源ID */
    private String resourceId;

    /** 资源名称 */
    private String resourceName;

    /** 学段 */
    private String stage;

    /** 状态机：PENDING → ORGANIZING → COMPLETED / FAILED */
    private String status;

    /** 阶段说明 / 失败信息 */
    private String message;

    /** 完成后的整理稿 Markdown */
    private String organizedMd;

    /** 完成后的原始提取文本（对照参考，与 KnowledgeAIDocVO 一致截断） */
    private String originalText;

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

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
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

    public String getOrganizedMd() {
        return organizedMd;
    }

    public void setOrganizedMd(String organizedMd) {
        this.organizedMd = organizedMd;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
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