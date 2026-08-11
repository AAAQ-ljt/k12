package com.smart.campus.web.entity.vo.studyplan;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StudyPlanDetailVO implements Serializable {

    private String planId;
    private String courseId;
    private String courseName;
    private String coverPath;
    private String description;
    private Integer status;
    private String statusText;
    private Integer taskCount;
    private Integer completedCount;
    private Integer progress;
    private List<StudyPlanDetailItemVO> itemList = new ArrayList<>();

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

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

    public String getCoverPath() {
        return coverPath;
    }

    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public Integer getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(Integer taskCount) {
        this.taskCount = taskCount;
    }

    public Integer getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(Integer completedCount) {
        this.completedCount = completedCount;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public List<StudyPlanDetailItemVO> getItemList() {
        return itemList;
    }

    public void setItemList(List<StudyPlanDetailItemVO> itemList) {
        this.itemList = itemList;
    }
}
