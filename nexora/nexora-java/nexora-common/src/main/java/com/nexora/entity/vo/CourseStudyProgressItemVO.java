package com.nexora.entity.vo;

import java.util.Date;

/**
 * 学习分析-课程学习进度明细
 */
public class CourseStudyProgressItemVO {

    private String courseId;
    private String courseName;
    private Integer studiedLessons;
    private Integer totalLessons;
    private Integer progress;
    private Long studyDuration;
    private Date finishTime;
    private Date updateTime;

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

    public Integer getStudiedLessons() {
        return studiedLessons;
    }

    public void setStudiedLessons(Integer studiedLessons) {
        this.studiedLessons = studiedLessons;
    }

    public Integer getTotalLessons() {
        return totalLessons;
    }

    public void setTotalLessons(Integer totalLessons) {
        this.totalLessons = totalLessons;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public Long getStudyDuration() {
        return studyDuration;
    }

    public void setStudyDuration(Long studyDuration) {
        this.studyDuration = studyDuration;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
