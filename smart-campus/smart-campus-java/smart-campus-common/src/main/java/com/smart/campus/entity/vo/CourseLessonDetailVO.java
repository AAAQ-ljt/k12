package com.smart.campus.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CourseLessonDetailVO implements Serializable {

    private String lessonId;

    private String lessonName;

    private Integer sortOrder;

    private Integer videoResourceId;

    private String videoResourceName;

    private String videoFilePath;

    private String videoCoverPath;

    private String paperId;

    private String paperName;

    private Integer paperType;

    private String paperTypeText;

    private Integer studySeconds;

    private Integer lastPositionSeconds;

    private Integer maxPositionSeconds;

    private Integer videoDurationSeconds;

    private Integer isCompleted;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastStudyTime;

    private List<CourseLessonResourceVO> coursewareList = new ArrayList<>();

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public String getLessonName() {
        return lessonName;
    }

    public void setLessonName(String lessonName) {
        this.lessonName = lessonName;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getVideoResourceId() {
        return videoResourceId;
    }

    public void setVideoResourceId(Integer videoResourceId) {
        this.videoResourceId = videoResourceId;
    }

    public String getVideoResourceName() {
        return videoResourceName;
    }

    public void setVideoResourceName(String videoResourceName) {
        this.videoResourceName = videoResourceName;
    }

    public String getVideoFilePath() {
        return videoFilePath;
    }

    public void setVideoFilePath(String videoFilePath) {
        this.videoFilePath = videoFilePath;
    }

    public String getVideoCoverPath() {
        return videoCoverPath;
    }

    public void setVideoCoverPath(String videoCoverPath) {
        this.videoCoverPath = videoCoverPath;
    }

    public String getPaperId() {
        return paperId;
    }

    public void setPaperId(String paperId) {
        this.paperId = paperId;
    }

    public String getPaperName() {
        return paperName;
    }

    public void setPaperName(String paperName) {
        this.paperName = paperName;
    }

    public Integer getPaperType() {
        return paperType;
    }

    public void setPaperType(Integer paperType) {
        this.paperType = paperType;
    }

    public String getPaperTypeText() {
        return paperTypeText;
    }

    public void setPaperTypeText(String paperTypeText) {
        this.paperTypeText = paperTypeText;
    }

    public Integer getStudySeconds() {
        return studySeconds;
    }

    public void setStudySeconds(Integer studySeconds) {
        this.studySeconds = studySeconds;
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

    public Date getLastStudyTime() {
        return lastStudyTime;
    }

    public void setLastStudyTime(Date lastStudyTime) {
        this.lastStudyTime = lastStudyTime;
    }

    public List<CourseLessonResourceVO> getCoursewareList() {
        return coursewareList;
    }

    public void setCoursewareList(List<CourseLessonResourceVO> coursewareList) {
        this.coursewareList = coursewareList;
    }
}
