package com.nexora.vo;

/**
 * 知识点掌握度明细项（学生端学习进度列表）
 */
public class KnowledgeMasteryItemVO {

    /** 知识点ID */
    private String knowledgePointId;

    /** 知识点名称（无对应知识点时回落ID） */
    private String knowledgePointName;

    /** 学段 */
    private String stage;

    /** 掌握度 0-100（历史正确率） */
    private Integer masteryScore;

    /** 状态：0未解锁 1进行中 2已掌握 */
    private Integer status;

    /** 练习次数 */
    private Integer practiceCount;

    /** 答对次数 */
    private Integer correctCount;

    /** 最近练习时间（yyyy-MM-dd HH:mm:ss） */
    private String lastPracticeTime;

    /** 下次复习时间（yyyy-MM-dd HH:mm:ss），未排复习为空 */
    private String nextReviewTime;

    /** 是否已到复习时间 */
    private Boolean due;

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

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public Integer getMasteryScore() {
        return masteryScore;
    }

    public void setMasteryScore(Integer masteryScore) {
        this.masteryScore = masteryScore;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getPracticeCount() {
        return practiceCount;
    }

    public void setPracticeCount(Integer practiceCount) {
        this.practiceCount = practiceCount;
    }

    public Integer getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(Integer correctCount) {
        this.correctCount = correctCount;
    }

    public String getLastPracticeTime() {
        return lastPracticeTime;
    }

    public void setLastPracticeTime(String lastPracticeTime) {
        this.lastPracticeTime = lastPracticeTime;
    }

    public String getNextReviewTime() {
        return nextReviewTime;
    }

    public void setNextReviewTime(String nextReviewTime) {
        this.nextReviewTime = nextReviewTime;
    }

    public Boolean getDue() {
        return due;
    }

    public void setDue(Boolean due) {
        this.due = due;
    }
}
