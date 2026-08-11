package com.smart.campus.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

public class CourseListItemVO implements Serializable {

    private String courseId;

    private String courseName;

    private Integer coverResourceId;

    private String coverPath;

    private Integer teacherId;

    private String teacherName;

    private String description;

    private Integer recordStatus;

    private Integer status;

    private Integer classCount;

    private String classNames;

    private Integer chapterCount;

    private Integer lessonCount;

    /**
     * 已完成课时数（含 Redis 缓存的最新进度）
     */
    private Integer completedLessonCount;

    /**
     * 学习进度百分比（0-100），按已完成课时数 / 总课时数 计算
     */
    private Integer progress;

    /**
     * 累计学习时长，单位秒（来自 course_study_progress，含 Redis 缓存）
     */
    private Integer studySeconds;

    /**
     * 当前所在章节序号（从 1 开始，无学习记录则为 0）
     */
    private Integer currentChapterIndex;

    /**
     * 学习状态: 0未开始 1学习中 2已完成
     */
    private Integer studyStatus;

    /**
     * 最后学习时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastStudyTime;

    private Integer isCollected;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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

    public Integer getCoverResourceId() {
        return coverResourceId;
    }

    public void setCoverResourceId(Integer coverResourceId) {
        this.coverResourceId = coverResourceId;
    }

    public String getCoverPath() {
        return coverPath;
    }

    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }

    public Integer getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Integer teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getRecordStatus() {
        return recordStatus;
    }

    public void setRecordStatus(Integer recordStatus) {
        this.recordStatus = recordStatus;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getClassCount() {
        return classCount;
    }

    public void setClassCount(Integer classCount) {
        this.classCount = classCount;
    }

    public String getClassNames() {
        return classNames;
    }

    public void setClassNames(String classNames) {
        this.classNames = classNames;
    }

    public Integer getChapterCount() {
        return chapterCount;
    }

    public void setChapterCount(Integer chapterCount) {
        this.chapterCount = chapterCount;
    }

    public Integer getLessonCount() {
        return lessonCount;
    }

    public void setLessonCount(Integer lessonCount) {
        this.lessonCount = lessonCount;
    }

    public Integer getCompletedLessonCount() {
        return completedLessonCount;
    }

    public void setCompletedLessonCount(Integer completedLessonCount) {
        this.completedLessonCount = completedLessonCount;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public Integer getStudySeconds() {
        return studySeconds;
    }

    public void setStudySeconds(Integer studySeconds) {
        this.studySeconds = studySeconds;
    }

    public Integer getCurrentChapterIndex() {
        return currentChapterIndex;
    }

    public void setCurrentChapterIndex(Integer currentChapterIndex) {
        this.currentChapterIndex = currentChapterIndex;
    }

    public Integer getStudyStatus() {
        return studyStatus;
    }

    public void setStudyStatus(Integer studyStatus) {
        this.studyStatus = studyStatus;
    }

    public Date getLastStudyTime() {
        return lastStudyTime;
    }

    public void setLastStudyTime(Date lastStudyTime) {
        this.lastStudyTime = lastStudyTime;
    }

    public Integer getIsCollected() {
        return isCollected;
    }

    public void setIsCollected(Integer isCollected) {
        this.isCollected = isCollected;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
