package com.nexora.admin.dto;

/**
 * 某难度下单一题型的生成数量
 */
public class QuestionTypeCountDTO {

    private Integer questionType;

    private Integer count;

    public Integer getQuestionType() {
        return questionType;
    }

    public void setQuestionType(Integer questionType) {
        this.questionType = questionType;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
