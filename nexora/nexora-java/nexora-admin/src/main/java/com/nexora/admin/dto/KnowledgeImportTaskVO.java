package com.nexora.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 知识库解析入库异步任务体/状态（Redis 持久化，前端轮询）。
 *
 * 状态机：PENDING → EXTRACTING(资源文本提取/分块准备) → VECTORIZING(分块向量化) → COMPLETED / FAILED；
 * 直录（手动内容 / 资源说明）跳过 EXTRACTING。同一 docId 运行中互斥，重复提交返回进行中任务。
 */
public class KnowledgeImportTaskVO {

    /** 任务ID */
    private String taskId;

    /** 文档ID */
    private String docId;

    /** 源资源ID（可为空，手动录入/资源说明无源资源） */
    private String resourceId;

    /** 来源：1 资料解析 2 资源说明 */
    private Integer sourceType;

    /** 状态机：PENDING → EXTRACTING → VECTORIZING → COMPLETED / FAILED */
    private String status;

    /** 阶段说明 / 失败信息 */
    private String message;

    /** 进度 0-100 */
    private Integer progress;

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

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public Integer getSourceType() {
        return sourceType;
    }

    public void setSourceType(Integer sourceType) {
        this.sourceType = sourceType;
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

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
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