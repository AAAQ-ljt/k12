package com.smart.campus.admin.entity.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PaperQuestionSnapshotVO implements Serializable {

    private Integer questionId;

    private Integer questionType;

    private String questionTypeText;

    private String questionTitle;

    private Integer difficultyLevel;

    private String difficultyLevelText;

    private List<Integer> questionImageResourceIdList = new ArrayList<>();

    private String correctAnswerText;

    private String answerAnalysis;

    private List<QuestionOptionVO> optionList = new ArrayList<>();

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

    public List<Integer> getQuestionImageResourceIdList() {
        return questionImageResourceIdList;
    }

    public void setQuestionImageResourceIdList(List<Integer> questionImageResourceIdList) {
        this.questionImageResourceIdList = questionImageResourceIdList;
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

    public List<QuestionOptionVO> getOptionList() {
        return optionList;
    }

    public void setOptionList(List<QuestionOptionVO> optionList) {
        this.optionList = optionList;
    }
}
