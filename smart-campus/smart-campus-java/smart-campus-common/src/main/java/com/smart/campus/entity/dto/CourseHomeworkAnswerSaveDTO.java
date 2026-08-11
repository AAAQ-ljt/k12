package com.smart.campus.entity.dto;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public class CourseHomeworkAnswerSaveDTO implements Serializable {

    @NotNull(message = "提交ID不能为空")
    private Long submitId;

    @NotNull(message = "试卷题目ID不能为空")
    private Integer paperQuestionId;

    @NotNull(message = "题目ID不能为空")
    private Integer questionId;

    private String answerContent;

    public Long getSubmitId() {
        return submitId;
    }

    public void setSubmitId(Long submitId) {
        this.submitId = submitId;
    }

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

    public String getAnswerContent() {
        return answerContent;
    }

    public void setAnswerContent(String answerContent) {
        this.answerContent = answerContent;
    }
}
