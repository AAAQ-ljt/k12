package com.nexora.vo;

/**
 * 课时通关测验状态（课程详情解锁展示）
 */
public class LessonQuizStatusVO {

    /** 课时ID */
    private String lessonId;

    /** 是否配置了测验 */
    private boolean hasQuiz;

    /** 是否严格门禁（通过才解锁下一课时） */
    private boolean strict;

    /** 当前学生是否已通过 */
    private boolean passed;

    /** 对当前学生是否解锁 */
    private boolean unlocked;

    /** 是否有过作答记录 */
    private boolean hasAttempt;

    /** 最近一次作答得分 */
    private Integer lastScore;

    /** 测验总分（客观+主观配置分值合计） */
    private Integer totalScore;

    /** 及格线 */
    private Integer passScore;

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public boolean isHasQuiz() {
        return hasQuiz;
    }

    public void setHasQuiz(boolean hasQuiz) {
        this.hasQuiz = hasQuiz;
    }

    public boolean isStrict() {
        return strict;
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public boolean isHasAttempt() {
        return hasAttempt;
    }

    public void setHasAttempt(boolean hasAttempt) {
        this.hasAttempt = hasAttempt;
    }

    public Integer getLastScore() {
        return lastScore;
    }

    public void setLastScore(Integer lastScore) {
        this.lastScore = lastScore;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public Integer getPassScore() {
        return passScore;
    }

    public void setPassScore(Integer passScore) {
        this.passScore = passScore;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }
}