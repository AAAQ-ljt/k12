package com.nexora.vo;

import java.util.List;

/**
 * 课时通关测验答题面板数据（不返回答案，答案与解析在提交结果中返回）
 */
public class LessonQuizVO {

    /** 课时ID */
    private String lessonId;

    /** 测验方式：1题库选题 2AI生成 */
    private Integer quizMode;

    /** 及格分（百分制） */
    private Integer passScore;

    /** 0宽松考核 1严格门禁 */
    private Integer unlockNext;

    /** 题目列表 */
    private List<Question> questions;

    public static class Question {
        private String questionId;
        private Integer questionType;
        private String title;
        private Integer score;
        private List<Option> options;

        public String getQuestionId() {
            return questionId;
        }

        public void setQuestionId(String questionId) {
            this.questionId = questionId;
        }

        public Integer getQuestionType() {
            return questionType;
        }

        public void setQuestionType(Integer questionType) {
            this.questionType = questionType;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Integer getScore() {
            return score;
        }

        public void setScore(Integer score) {
            this.score = score;
        }

        public List<Option> getOptions() {
            return options;
        }

        public void setOptions(List<Option> options) {
            this.options = options;
        }
    }

    public static class Option {
        private Integer optionId;
        private String optionLabel;
        private String optionContent;

        public Integer getOptionId() {
            return optionId;
        }

        public void setOptionId(Integer optionId) {
            this.optionId = optionId;
        }

        public String getOptionLabel() {
            return optionLabel;
        }

        public void setOptionLabel(String optionLabel) {
            this.optionLabel = optionLabel;
        }

        public String getOptionContent() {
            return optionContent;
        }

        public void setOptionContent(String optionContent) {
            this.optionContent = optionContent;
        }
    }

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

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}