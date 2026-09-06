package com.nexora.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;
import java.util.List;

/**
 * 课时通关测验提交判分结果
 */
public class LessonQuizSubmitResultVO {

    /** 是否通过（达标） */
    private boolean passed;

    /** 答对题数 */
    private int correctCount;

    /** 总题数 */
    private int totalCount;

    /** 得分 */
    private int score;

    /** 满分 */
    private int totalScore;

    /** 及格线 */
    private int passScore;

    /** 逐题判分明细 */
    private List<QuestionResult> results;

    /** 最近一次提交时间（结果回显时返回） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date submitTime;

    public static class QuestionResult {
        private String questionId;
        private String title;
        private String userAnswer;
        private String correctAnswer;
        private boolean correct;
        /** 主观题（简答/解答/论述/材料）：不自动判分，需人工核对参考答案 */
        private boolean subjective;
        private int score;
        /** 该题满分（主观题也返回其配置分值） */
        private int questionScore;
        private String analysis;
        /** 题型（结果页按题型渲染选项/对比块） */
        private Integer questionType;
        /** 题目选项（结果页重渲染高亮用，提交判分时同样返回） */
        private List<QuestionOption> options;
        /** 主观题批阅状态：0待批阅 1已批阅（结果回显时返回） */
        private Integer reviewStatus;
        /** 主观题人工批阅得分 */
        private Integer reviewScore;
        /** 主观题批阅评语 */
        private String reviewComment;

        public String getQuestionId() {
            return questionId;
        }

        public void setQuestionId(String questionId) {
            this.questionId = questionId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getUserAnswer() {
            return userAnswer;
        }

        public void setUserAnswer(String userAnswer) {
            this.userAnswer = userAnswer;
        }

        public String getCorrectAnswer() {
            return correctAnswer;
        }

        public void setCorrectAnswer(String correctAnswer) {
            this.correctAnswer = correctAnswer;
        }

        public boolean isCorrect() {
            return correct;
        }

        public void setCorrect(boolean correct) {
            this.correct = correct;
        }

        public boolean isSubjective() {
            return subjective;
        }

        public void setSubjective(boolean subjective) {
            this.subjective = subjective;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public int getQuestionScore() {
            return questionScore;
        }

        public void setQuestionScore(int questionScore) {
            this.questionScore = questionScore;
        }

        public String getAnalysis() {
            return analysis;
        }

        public void setAnalysis(String analysis) {
            this.analysis = analysis;
        }

        public Integer getQuestionType() {
            return questionType;
        }

        public void setQuestionType(Integer questionType) {
            this.questionType = questionType;
        }

        public List<QuestionOption> getOptions() {
            return options;
        }

        public void setOptions(List<QuestionOption> options) {
            this.options = options;
        }

        public Integer getReviewStatus() {
            return reviewStatus;
        }

        public void setReviewStatus(Integer reviewStatus) {
            this.reviewStatus = reviewStatus;
        }

        public Integer getReviewScore() {
            return reviewScore;
        }

        public void setReviewScore(Integer reviewScore) {
            this.reviewScore = reviewScore;
        }

        public String getReviewComment() {
            return reviewComment;
        }

        public void setReviewComment(String reviewComment) {
            this.reviewComment = reviewComment;
        }
    }

    /**
     * 题目选项（结果页渲染）
     */
    public static class QuestionOption {
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

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(int correctCount) {
        this.correctCount = correctCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public int getPassScore() {
        return passScore;
    }

    public void setPassScore(int passScore) {
        this.passScore = passScore;
    }

    public List<QuestionResult> getResults() {
        return results;
    }

    public void setResults(List<QuestionResult> results) {
        this.results = results;
    }

    public Date getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
    }
}