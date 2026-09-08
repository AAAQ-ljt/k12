package com.nexora.admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * 课时测验 AI 出题异步任务状态（Redis 持久化，TTL 2h）
 */
public class LessonQuizTaskVO {

    private String taskId;

    private String lessonId;

    /** 课程ID（写测验配置用） */
    private String courseId;

    /** PENDING 排队中 / GENERATING 生成中 / SUCCESS 完成 / FAILED 失败 */
    private String status;

    /** 当前步骤描述（进度条文案） */
    private String step;

    /** 已生成题数 */
    private Integer generated;

    /** 总题数 */
    private Integer total;

    /** 失败原因 */
    private String message;

    // ---- 任务参数快照（执行使用） ----

    private String stage;

    private String grade;

    private String topic;

    private String knowledgePointId;

    /** 挂载知识点名称（出题强锚定，防提示词跑偏） */
    private String knowledgePointName;

    private Integer difficulty;

    private Integer passScore;

    private Integer unlockNext;

    private Boolean partialCredit;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public Integer getGenerated() {
        return generated;
    }

    public void setGenerated(Integer generated) {
        this.generated = generated;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
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

    public Integer getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getPassScore() {
        return passScore;
    }

    public void setPassScore(Integer passScore) {
        this.passScore = passScore;
    }

    public Integer getUnlockNext() {
        return unlockNext;
    }

    public void setUnlockNext(Integer unlockNext) {
        this.unlockNext = unlockNext;
    }

    public Boolean getPartialCredit() {
        return partialCredit;
    }

    public void setPartialCredit(Boolean partialCredit) {
        this.partialCredit = partialCredit;
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
