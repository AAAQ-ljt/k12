package com.nexora.dto;

/**
 * 课时测验单题作答
 */
public class LessonQuizAnswerDTO {

    /** 题目ID */
    private String questionId;

    /** 学生作答（选项字母，如 A/B/C/D） */
    private String answer;

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}