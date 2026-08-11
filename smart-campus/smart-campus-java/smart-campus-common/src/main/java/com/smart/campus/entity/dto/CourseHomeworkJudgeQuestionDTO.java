package com.smart.campus.entity.dto;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;

public class CourseHomeworkJudgeQuestionDTO implements Serializable {

    @NotNull(message = "题目ID不能为空")
    private Integer questionId;

    @NotNull(message = "题目得分不能为空")
    private BigDecimal score;

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }
}
