package com.smart.campus.admin.entity.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class QuestionSaveDTO implements Serializable {

    public interface Create {}

    public interface Update extends Create {}

    @NotNull(message = "题目ID不能为空", groups = Update.class)
    private Integer questionId;

    @NotNull(message = "题目类型不能为空", groups = {Create.class, Update.class})
    private Integer questionType;

    @NotBlank(message = "题目标题不能为空", groups = {Create.class, Update.class})
    private String questionTitle;

    private List<Integer> questionImageResourceIdList = new ArrayList<>();

    @NotNull(message = "难度等级不能为空", groups = {Create.class, Update.class})
    private Integer difficultyLevel;

    private List<String> correctOptionKeyList = new ArrayList<>();

    private String correctAnswerText;

    private String answerAnalysis;

    @Valid
    private List<QuestionOptionSaveDTO> optionList = new ArrayList<>();

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

    public List<String> getCorrectOptionKeyList() {
        return correctOptionKeyList;
    }

    public void setCorrectOptionKeyList(List<String> correctOptionKeyList) {
        this.correctOptionKeyList = correctOptionKeyList;
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

    public List<QuestionOptionSaveDTO> getOptionList() {
        return optionList;
    }

    public void setOptionList(List<QuestionOptionSaveDTO> optionList) {
        this.optionList = optionList;
    }
}
