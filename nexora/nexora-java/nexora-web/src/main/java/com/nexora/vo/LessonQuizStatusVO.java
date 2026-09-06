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

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }
}