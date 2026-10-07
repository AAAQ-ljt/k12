package com.nexora.entity.vo;

import java.util.Date;

/**
 * 学习分析-知识点掌握度明细
 */
public class KnowledgeMasteryVO {

    private String knowledgePointId;
    private String knowledgePointName;
    private Integer masteryScore;
    private Integer status;
    private Integer practiceCount;
    private Integer correctCount;
    private Double accuracy;
    private Date lastPracticeTime;
    private Date nextReviewTime;

    /** 是否不再提醒复习：0正常 1长期静音（复习闭环设计点④） */
    private Integer muted;

    /** 临时静音到期时间（到点恢复提醒） */
    private Date muteUntil;

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

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public Date getLastPracticeTime() {
        return lastPracticeTime;
    }

    public void setLastPracticeTime(Date lastPracticeTime) {
        this.lastPracticeTime = lastPracticeTime;
    }

    public Date getNextReviewTime() {
        return nextReviewTime;
    }

    public Integer getMuted() {
        return muted;
    }

    public void setMuted(Integer muted) {
        this.muted = muted;
    }

    public Date getMuteUntil() {
        return muteUntil;
    }

    public void setMuteUntil(Date muteUntil) {
        this.muteUntil = muteUntil;
    }

    public void setNextReviewTime(Date nextReviewTime) {
        this.nextReviewTime = nextReviewTime;
    }
}
