package com.smart.campus.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

public class CourseStudyReportVO implements Serializable {

    private String courseId;

    private String chapterId;

    private String lessonId;

    private Integer courseStudySeconds;

    private Integer courseStatus;

    private String currentChapterId;

    private String currentLessonId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date courseLastStudyTime;

    private Integer lessonStudySeconds;

    private Integer lastPositionSeconds;

    private Integer maxPositionSeconds;

    private Integer videoDurationSeconds;

    private Integer isCompleted;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lessonLastStudyTime;

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getChapterId() {
        return chapterId;
    }

    public void setChapterId(String chapterId) {
        this.chapterId = chapterId;
    }

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public Integer getCourseStudySeconds() {
        return courseStudySeconds;
    }

    public void setCourseStudySeconds(Integer courseStudySeconds) {
        this.courseStudySeconds = courseStudySeconds;
    }

    public Integer getCourseStatus() {
        return courseStatus;
    }

    public void setCourseStatus(Integer courseStatus) {
        this.courseStatus = courseStatus;
    }

    public String getCurrentChapterId() {
        return currentChapterId;
    }

    public void setCurrentChapterId(String currentChapterId) {
        this.currentChapterId = currentChapterId;
    }

    public String getCurrentLessonId() {
        return currentLessonId;
    }

    public void setCurrentLessonId(String currentLessonId) {
        this.currentLessonId = currentLessonId;
    }

    public Date getCourseLastStudyTime() {
        return courseLastStudyTime;
    }

    public void setCourseLastStudyTime(Date courseLastStudyTime) {
        this.courseLastStudyTime = courseLastStudyTime;
    }

    public Integer getLessonStudySeconds() {
        return lessonStudySeconds;
    }

    public void setLessonStudySeconds(Integer lessonStudySeconds) {
        this.lessonStudySeconds = lessonStudySeconds;
    }

    public Integer getLastPositionSeconds() {
        return lastPositionSeconds;
    }

    public void setLastPositionSeconds(Integer lastPositionSeconds) {
        this.lastPositionSeconds = lastPositionSeconds;
    }

    public Integer getMaxPositionSeconds() {
        return maxPositionSeconds;
    }

    public void setMaxPositionSeconds(Integer maxPositionSeconds) {
        this.maxPositionSeconds = maxPositionSeconds;
    }

    public Integer getVideoDurationSeconds() {
        return videoDurationSeconds;
    }

    public void setVideoDurationSeconds(Integer videoDurationSeconds) {
        this.videoDurationSeconds = videoDurationSeconds;
    }

    public Integer getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(Integer isCompleted) {
        this.isCompleted = isCompleted;
    }

    public Date getLessonLastStudyTime() {
        return lessonLastStudyTime;
    }

    public void setLessonLastStudyTime(Date lessonLastStudyTime) {
        this.lessonLastStudyTime = lessonLastStudyTime;
    }
}
