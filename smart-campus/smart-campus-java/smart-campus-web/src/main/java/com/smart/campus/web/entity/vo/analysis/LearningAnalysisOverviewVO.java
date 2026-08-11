package com.smart.campus.web.entity.vo.analysis;

import java.io.Serializable;
import java.math.BigDecimal;

public class LearningAnalysisOverviewVO implements Serializable {

    private BigDecimal totalStudyHours;
    private BigDecimal previousStudyHours;
    private Integer hoursGrowthRate;
    private Integer totalTaskCount;
    private Integer completedTaskCount;
    private Integer courseCount;
    private Integer inProgressCourseCount;
    private Integer averageCourseProgress;
    private BigDecimal averageScore;
    private Integer completedExamCount;
    private Integer totalPlanCount;
    private Integer inProgressPlanCount;

    public BigDecimal getTotalStudyHours() {
        return totalStudyHours;
    }

    public void setTotalStudyHours(BigDecimal totalStudyHours) {
        this.totalStudyHours = totalStudyHours;
    }

    public BigDecimal getPreviousStudyHours() {
        return previousStudyHours;
    }

    public void setPreviousStudyHours(BigDecimal previousStudyHours) {
        this.previousStudyHours = previousStudyHours;
    }

    public Integer getHoursGrowthRate() {
        return hoursGrowthRate;
    }

    public void setHoursGrowthRate(Integer hoursGrowthRate) {
        this.hoursGrowthRate = hoursGrowthRate;
    }

    public Integer getTotalTaskCount() {
        return totalTaskCount;
    }

    public void setTotalTaskCount(Integer totalTaskCount) {
        this.totalTaskCount = totalTaskCount;
    }

    public Integer getCompletedTaskCount() {
        return completedTaskCount;
    }

    public void setCompletedTaskCount(Integer completedTaskCount) {
        this.completedTaskCount = completedTaskCount;
    }

    public Integer getCourseCount() {
        return courseCount;
    }

    public void setCourseCount(Integer courseCount) {
        this.courseCount = courseCount;
    }

    public Integer getInProgressCourseCount() {
        return inProgressCourseCount;
    }

    public void setInProgressCourseCount(Integer inProgressCourseCount) {
        this.inProgressCourseCount = inProgressCourseCount;
    }

    public Integer getAverageCourseProgress() {
        return averageCourseProgress;
    }

    public void setAverageCourseProgress(Integer averageCourseProgress) {
        this.averageCourseProgress = averageCourseProgress;
    }

    public BigDecimal getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(BigDecimal averageScore) {
        this.averageScore = averageScore;
    }

    public Integer getCompletedExamCount() {
        return completedExamCount;
    }

    public void setCompletedExamCount(Integer completedExamCount) {
        this.completedExamCount = completedExamCount;
    }

    public Integer getTotalPlanCount() {
        return totalPlanCount;
    }

    public void setTotalPlanCount(Integer totalPlanCount) {
        this.totalPlanCount = totalPlanCount;
    }

    public Integer getInProgressPlanCount() {
        return inProgressPlanCount;
    }

    public void setInProgressPlanCount(Integer inProgressPlanCount) {
        this.inProgressPlanCount = inProgressPlanCount;
    }
}
