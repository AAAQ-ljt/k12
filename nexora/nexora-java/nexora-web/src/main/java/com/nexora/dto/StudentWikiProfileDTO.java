package com.nexora.dto;

/**
 * 学生学习档案（个人 Wiki 用户视图）入参
 */
public class StudentWikiProfileDTO {

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
}