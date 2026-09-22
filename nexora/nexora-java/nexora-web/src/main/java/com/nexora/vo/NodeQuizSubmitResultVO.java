package com.nexora.vo;

import java.util.List;

/**
 * 节点快测提交判分结果。
 *
 * 判分口径：仅客观题自动判分；本次得分率 = 答对题数 ÷ 总题数 × 100；
 * 「本次通过」= 得分率 ≥ 80（快测固定 3 题，即须全对才判通过）。全对一轮 → 练习次数累计 ≥3 且分数 ≥80 → 跨入已掌握。
 */
public class NodeQuizSubmitResultVO {

    /** 本次是否通过（得分率 ≥ 80） */
    private boolean passed;

    /** 答对题数 */
    private int correctCount;

    /** 总题数 */
    private int totalCount;

    /** 本次得分率（百分制） */
    private int score;

    /** 回写后掌握度（0-100） */
    private int masteryScore;

    /** 是否已跨入「已掌握」 */
    private boolean mastered;

    /** 逐题判分明细 */
    private List<QuestionResult> results;

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

    public int getMasteryScore() {
        return masteryScore;
    }

    public void setMasteryScore(int masteryScore) {
        this.masteryScore = masteryScore;
    }

    public boolean isMastered() {
        return mastered;
    }

    public void setMastered(boolean mastered) {
        this.mastered = mastered;
    }

    public List<QuestionResult> getResults() {
        return results;
    }

    public void setResults(List<QuestionResult> results) {
        this.results = results;
    }

    public static class QuestionResult {

        /** 题号（0 起） */
        private int index;

        /** 题干 */
        private String question;

        /** 选项文本列表 */
        private List<String> options;

        /** 学生作答（所选选项文本） */
        private String userAnswer;

        /** 正确答案（选项文本） */
        private String correctAnswer;

        /** 是否答对 */
        private boolean correct;

        /** 解析 */
        private String analysis;

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public List<String> getOptions() {
            return options;
        }

        public void setOptions(List<String> options) {
            this.options = options;
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

        public String getAnalysis() {
            return analysis;
        }

        public void setAnalysis(String analysis) {
            this.analysis = analysis;
        }
    }
}