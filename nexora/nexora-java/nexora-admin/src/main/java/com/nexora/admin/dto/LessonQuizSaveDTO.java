package com.nexora.admin.dto;

import java.util.List;
import java.util.Map;

/**
 * 课时通关测验配置保存请求
 */
public class LessonQuizSaveDTO {

    /** 课时ID */
    private String lessonId;

    /** 0关闭 1题库选题 2AI生成 */
    private Integer quizMode;

    /** 题库模式已选题ID */
    private List<String> questionIds;

    /** 每题分值（questionId → 分），总分通常配到 100 */
    private Map<String, Integer> questionScores;

    /** AI模式题量 */
    private Integer questionCount;

    /** AI模式难度 1-3 */
    private Integer difficulty;

    /** 及格分（百分制） */
    private Integer passScore;

    /** 0宽松考核 1严格门禁 */
    private Integer unlockNext;

    /** AI 出题主题/知识点描述（AI 模式时必填） */
    private String topic;

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public Integer getQuizMode() {
        return quizMode;
    }

    public void setQuizMode(Integer quizMode) {
        this.quizMode = quizMode;
    }

    public List<String> getQuestionIds() {
        return questionIds;
    }

    public void setQuestionIds(List<String> questionIds) {
        this.questionIds = questionIds;
    }

    public Map<String, Integer> getQuestionScores() {
        return questionScores;
    }

    public void setQuestionScores(Map<String, Integer> questionScores) {
        this.questionScores = questionScores;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
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

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}