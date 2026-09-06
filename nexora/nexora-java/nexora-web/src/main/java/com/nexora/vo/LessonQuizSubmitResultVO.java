package com.nexora.vo;

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
}