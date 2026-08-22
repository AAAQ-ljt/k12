package com.nexora.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 学生学习档案（个人Wiki用户视图）
 */
public class UserWikiProfile implements Serializable {

    /**
     * 学生ID
     */
    private String userId;

    /**
     * 学习目标
     */
    private String learningGoal;

    /**
     * 关键问题（多个用分号分隔）
     */
    private String keyQuestions;

    /**
     * 感兴趣学科/主题（多个用分号分隔）
     */
    private String interestSubjects;

    /**
     * 自己的术语叫法（多个用分号分隔）
     */
    private String aliasTerms;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLearningGoal() {
        return learningGoal;
    }

    public void setLearningGoal(String learningGoal) {
        this.learningGoal = learningGoal;
    }

    public String getKeyQuestions() {
        return keyQuestions;
    }

    public void setKeyQuestions(String keyQuestions) {
        this.keyQuestions = keyQuestions;
    }

    public String getInterestSubjects() {
        return interestSubjects;
    }

    public void setInterestSubjects(String interestSubjects) {
        this.interestSubjects = interestSubjects;
    }

    public String getAliasTerms() {
        return aliasTerms;
    }

    public void setAliasTerms(String aliasTerms) {
        this.aliasTerms = aliasTerms;
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