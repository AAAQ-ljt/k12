package com.smart.campus.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public class CourseStudyProgressReportDTO implements Serializable {

    @NotBlank(message = "课程ID不能为空")
    private String courseId;

    @NotBlank(message = "章节ID不能为空")
    private String chapterId;

    @NotBlank(message = "课时ID不能为空")
    private String lessonId;

    @NotBlank(message = "学习会话ID不能为空")
    private String sessionId;

    private Integer videoResourceId;

    @NotNull(message = "本次学习时长不能为空")
    private Integer watchSeconds;

    @NotNull(message = "播放位置不能为空")
    private Integer positionSeconds;

    @NotNull(message = "视频总时长不能为空")
    private Integer durationSeconds;

    private Integer forceComplete;

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

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getVideoResourceId() {
        return videoResourceId;
    }

    public void setVideoResourceId(Integer videoResourceId) {
        this.videoResourceId = videoResourceId;
    }

    public Integer getWatchSeconds() {
        return watchSeconds;
    }

    public void setWatchSeconds(Integer watchSeconds) {
        this.watchSeconds = watchSeconds;
    }

    public Integer getPositionSeconds() {
        return positionSeconds;
    }

    public void setPositionSeconds(Integer positionSeconds) {
        this.positionSeconds = positionSeconds;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Integer getForceComplete() {
        return forceComplete;
    }

    public void setForceComplete(Integer forceComplete) {
        this.forceComplete = forceComplete;
    }
}
