package com.nexora.vo;

/**
 * 我的课程学习进度（course_study_lesson_progress 聚合）
 */
public class CourseProgressVO {

    /** 课程ID */
    private String courseId;

    /** 课程名 */
    private String courseName;

    /** 封面 */
    private String cover;

    /** 学段 */
    private String stage;

    /** 总课时数 */
    private int lessonCount;

    /** 已完成课时数 */
    private int finishedLessons;

    /** 完成进度 0-100（lessonCount 未知或为 0 时为 0） */
    private int progress;

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public int getLessonCount() {
        return lessonCount;
    }

    public void setLessonCount(int lessonCount) {
        this.lessonCount = lessonCount;
    }

    public int getFinishedLessons() {
        return finishedLessons;
    }

    public void setFinishedLessons(int finishedLessons) {
        this.finishedLessons = finishedLessons;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}