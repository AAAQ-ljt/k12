package com.smart.campus.admin.entity.vo;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardVO {

    private List<AdminDashboardMetricVO> metricCards = new ArrayList<>();

    private List<AdminDashboardTrendVO> teachingTrend = new ArrayList<>();

    private List<AdminDashboardResourceStatVO> resourceStats = new ArrayList<>();

    private List<AdminDashboardTodoVO> todoList = new ArrayList<>();

    private List<AdminDashboardActivityVO> activityList = new ArrayList<>();

    private Integer totalResourceCount = 0;

    private Integer storageUsagePercent = 0;

    public List<AdminDashboardMetricVO> getMetricCards() {
        return metricCards;
    }

    public void setMetricCards(List<AdminDashboardMetricVO> metricCards) {
        this.metricCards = metricCards;
    }

    public List<AdminDashboardTrendVO> getTeachingTrend() {
        return teachingTrend;
    }

    public void setTeachingTrend(List<AdminDashboardTrendVO> teachingTrend) {
        this.teachingTrend = teachingTrend;
    }

    public List<AdminDashboardResourceStatVO> getResourceStats() {
        return resourceStats;
    }

    public void setResourceStats(List<AdminDashboardResourceStatVO> resourceStats) {
        this.resourceStats = resourceStats;
    }

    public List<AdminDashboardTodoVO> getTodoList() {
        return todoList;
    }

    public void setTodoList(List<AdminDashboardTodoVO> todoList) {
        this.todoList = todoList;
    }

    public List<AdminDashboardActivityVO> getActivityList() {
        return activityList;
    }

    public void setActivityList(List<AdminDashboardActivityVO> activityList) {
        this.activityList = activityList;
    }

    public Integer getTotalResourceCount() {
        return totalResourceCount;
    }

    public void setTotalResourceCount(Integer totalResourceCount) {
        this.totalResourceCount = totalResourceCount;
    }

    public Integer getStorageUsagePercent() {
        return storageUsagePercent;
    }

    public void setStorageUsagePercent(Integer storageUsagePercent) {
        this.storageUsagePercent = storageUsagePercent;
    }
}
