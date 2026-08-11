package com.smart.campus.web.entity.vo.studyplan;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class StudyPlanDashboardVO implements Serializable {

    private Integer totalPlanCount;
    private Integer totalTaskCount;
    private Integer completedTaskCount;
    private Integer inProgressPlanCount;
    private BigDecimal totalStudyHours;
    private List<StudyPlanScheduleItemVO> todayPlanList = new ArrayList<>();
    private List<StudyPlanScheduleItemVO> calendarPlanList = new ArrayList<>();
    private List<StudyPlanListItemVO> planList = new ArrayList<>();

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

    public Integer getInProgressPlanCount() {
        return inProgressPlanCount;
    }

    public void setInProgressPlanCount(Integer inProgressPlanCount) {
        this.inProgressPlanCount = inProgressPlanCount;
    }

    public BigDecimal getTotalStudyHours() {
        return totalStudyHours;
    }

    public void setTotalStudyHours(BigDecimal totalStudyHours) {
        this.totalStudyHours = totalStudyHours;
    }

    public List<StudyPlanScheduleItemVO> getTodayPlanList() {
        return todayPlanList;
    }

    public void setTodayPlanList(List<StudyPlanScheduleItemVO> todayPlanList) {
        this.todayPlanList = todayPlanList;
    }

    public List<StudyPlanScheduleItemVO> getCalendarPlanList() {
        return calendarPlanList;
    }

    public void setCalendarPlanList(List<StudyPlanScheduleItemVO> calendarPlanList) {
        this.calendarPlanList = calendarPlanList;
    }

    public List<StudyPlanListItemVO> getPlanList() {
        return planList;
    }

    public void setPlanList(List<StudyPlanListItemVO> planList) {
        this.planList = planList;
    }
}
