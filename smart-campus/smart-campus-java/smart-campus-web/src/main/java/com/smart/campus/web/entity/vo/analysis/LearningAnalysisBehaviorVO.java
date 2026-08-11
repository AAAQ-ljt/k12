package com.smart.campus.web.entity.vo.analysis;

import java.io.Serializable;

public class LearningAnalysisBehaviorVO implements Serializable {

    private Integer taskCompletionRate;
    private Integer activeDays;
    private Integer completedExamCount;
    private Integer totalPlanCount;
    private Integer totalTaskCount;
    private Integer completedTaskCount;

    public Integer getTaskCompletionRate() {
        return taskCompletionRate;
    }

    public void setTaskCompletionRate(Integer taskCompletionRate) {
        this.taskCompletionRate = taskCompletionRate;
    }

    public Integer getActiveDays() {
        return activeDays;
    }

    public void setActiveDays(Integer activeDays) {
        this.activeDays = activeDays;
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
}
