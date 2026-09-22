package com.nexora.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 学习路径节点快测异步出题任务体/状态（Redis 持久化，前端轮询）。
 *
 * 状态机：PENDING → QUIZ_GENERATING → COMPLETED / FAILED。
 * 生成完成时 quizJson 承载题目（含答案与解析，与 QuizScript.toJson() 同构），
 * 前端解析后进入答题卡；任务期间节点快测按钮禁用、后端按用户互斥，防止并发重复出题。
 */
public class NodeQuizTaskVO {

    /** 任务ID */
    private String taskId;

    /** 学生ID */
    private String userId;

    /** 学段（出题按学段适配） */
    private String stage;

    /** 节点ID */
    private String itemId;

    /** 知识点ID */
    private String knowledgePointId;

    /** 知识点/主题名（出题主题） */
    private String knowledgePointName;

    /** 状态机：PENDING → QUIZ_GENERATING → COMPLETED / FAILED */
    private String status;

    /** 任务说明/错误信息 */
    private String message;

    /** 完成后的题目 JSON（含答案与解析） */
    private String quizJson;

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

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getKnowledgePointId() {
        return knowledgePointId;
    }

    public void setKnowledgePointId(String knowledgePointId) {
        this.knowledgePointId = knowledgePointId;
    }

    public String getKnowledgePointName() {
        return knowledgePointName;
    }

    public void setKnowledgePointName(String knowledgePointName) {
        this.knowledgePointName = knowledgePointName;
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

    public String getQuizJson() {
        return quizJson;
    }

    public void setQuizJson(String quizJson) {
        this.quizJson = quizJson;
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