package com.smart.campus.admin.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QuestionDetailVO implements Serializable {

    private Integer questionId;

    private Integer questionType;

    private String questionTitle;

    private List<Integer> questionImageResourceIdList = new ArrayList<>();

    private Integer difficultyLevel;

    private String correctAnswerText;

    private List<String> correctOptionKeyList = new ArrayList<>();

    private String answerAnalysis;

    private List<QuestionOptionVO> optionList = new ArrayList<>();

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

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

    public String getQuestionTitle() {
        return questionTitle;
    }

    public void setQuestionTitle(String questionTitle) {
        this.questionTitle = questionTitle;
    }

    public List<Integer> getQuestionImageResourceIdList() {
        return questionImageResourceIdList;
    }

    public void setQuestionImageResourceIdList(List<Integer> questionImageResourceIdList) {
        this.questionImageResourceIdList = questionImageResourceIdList;
    }

    public Integer getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(Integer difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public String getCorrectAnswerText() {
        return correctAnswerText;
    }

    public void setCorrectAnswerText(String correctAnswerText) {
        this.correctAnswerText = correctAnswerText;
    }

    public List<String> getCorrectOptionKeyList() {
        return correctOptionKeyList;
    }

    public void setCorrectOptionKeyList(List<String> correctOptionKeyList) {
        this.correctOptionKeyList = correctOptionKeyList;
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
