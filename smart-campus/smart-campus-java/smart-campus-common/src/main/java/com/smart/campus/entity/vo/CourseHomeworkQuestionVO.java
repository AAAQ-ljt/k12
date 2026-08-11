package com.smart.campus.entity.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CourseHomeworkQuestionVO implements Serializable {

    private Integer paperQuestionId;

    private Integer questionId;

    private Integer questionType;

    private String questionTypeText;

    private String questionTitle;

    private Integer difficultyLevel;

    private String difficultyLevelText;

    private String correctAnswerText;

    private String answerAnalysis;

    private BigDecimal questionScore;

    private Integer sortOrder;

    private String answerContent;

    private BigDecimal finalScore;

    private Integer judgeStatus;

    private Boolean answered;

    private List<Integer> questionImageResourceIdList = new ArrayList<>();

    private List<CourseAssessmentQuestionOptionVO> optionList = new ArrayList<>();

    public Integer getPaperQuestionId() {
        return paperQuestionId;
    }

    public void setPaperQuestionId(Integer paperQuestionId) {
        this.paperQuestionId = paperQuestionId;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public Integer getQuestionType() {
        return questionType;
    }

    public void setQuestionType(Integer questionType) {
        this.questionType = questionType;
    }

    public String getQuestionTypeText() {
        return questionTypeText;
    }

    public void setQuestionTypeText(String questionTypeText) {
        this.questionTypeText = questionTypeText;
    }

    public String getQuestionTitle() {
        return questionTitle;
    }

    public void setQuestionTitle(String questionTitle) {
        this.questionTitle = questionTitle;
    }

    public Integer getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(Integer difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public String getDifficultyLevelText() {
        return difficultyLevelText;
    }

    public void setDifficultyLevelText(String difficultyLevelText) {
        this.difficultyLevelText = difficultyLevelText;
    }

    public String getCorrectAnswerText() {
        return correctAnswerText;
    }

    public void setCorrectAnswerText(String correctAnswerText) {
        this.correctAnswerText = correctAnswerText;
    }

    public String getAnswerAnalysis() {
        return answerAnalysis;
    }

    public void setAnswerAnalysis(String answerAnalysis) {
        this.answerAnalysis = answerAnalysis;
    }

    public BigDecimal getQuestionScore() {
        return questionScore;
    }

    public void setQuestionScore(BigDecimal questionScore) {
        this.questionScore = questionScore;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getAnswerContent() {
        return answerContent;
    }

    public void setAnswerContent(String answerContent) {
        this.answerContent = answerContent;
    }

    public BigDecimal getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(BigDecimal finalScore) {
        this.finalScore = finalScore;
    }

    public Integer getJudgeStatus() {
        return judgeStatus;
    }

    public void setJudgeStatus(Integer judgeStatus) {
        this.judgeStatus = judgeStatus;
    }

    public Boolean getAnswered() {
        return answered;
    }

    public void setAnswered(Boolean answered) {
        this.answered = answered;
    }

    public List<Integer> getQuestionImageResourceIdList() {
        return questionImageResourceIdList;
    }

    public void setQuestionImageResourceIdList(List<Integer> questionImageResourceIdList) {
        this.questionImageResourceIdList = questionImageResourceIdList;
    }

    public List<CourseAssessmentQuestionOptionVO> getOptionList() {
        return optionList;
    }

    public void setOptionList(List<CourseAssessmentQuestionOptionVO> optionList) {
        this.optionList = optionList;
    }
}
