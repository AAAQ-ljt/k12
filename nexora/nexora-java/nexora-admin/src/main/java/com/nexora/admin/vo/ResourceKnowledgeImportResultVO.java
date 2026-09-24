package com.nexora.admin.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * 资源解析入库结果。
 */
public class ResourceKnowledgeImportResultVO {

    private String docId;
    private String title;
    private String stage;
    private String knowledgePointId;
    private Integer difficulty;
    private Integer sourceType;
    private String sourceResourceId;
    private Integer contentLength;
    private Integer chunkCount;
    private Integer vectorStatus;
    private List<String> warnings = new ArrayList<>();
    private Boolean async;
    /** 解析入库任务ID（异步任务状态机轮询用） */
    private String taskId;
    /** 任务状态：PENDING / EXTRACTING / VECTORIZING / COMPLETED / FAILED */
    private String taskStatus;
    /** 任务进度 0-100 */
    private Integer progress;
    /** 任务阶段说明 / 失败信息 */
    private String message;

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getKnowledgePointId() {
        return knowledgePointId;
    }

    public void setKnowledgePointId(String knowledgePointId) {
        this.knowledgePointId = knowledgePointId;
    }

    public Integer getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getSourceType() {
        return sourceType;
    }

    public void setSourceType(Integer sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceResourceId() {
        return sourceResourceId;
    }

    public void setSourceResourceId(String sourceResourceId) {
        this.sourceResourceId = sourceResourceId;
    }

    public Integer getContentLength() {
        return contentLength;
    }

    public void setContentLength(Integer contentLength) {
        this.contentLength = contentLength;
    }

    public Integer getChunkCount() {
        return chunkCount;
    }

    public void setChunkCount(Integer chunkCount) {
        this.chunkCount = chunkCount;
    }

    public Integer getVectorStatus() {
        return vectorStatus;
    }

    public void setVectorStatus(Integer vectorStatus) {
        this.vectorStatus = vectorStatus;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public Boolean getAsync() {
        return async;
    }

    public void setAsync(Boolean async) {
        this.async = async;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
