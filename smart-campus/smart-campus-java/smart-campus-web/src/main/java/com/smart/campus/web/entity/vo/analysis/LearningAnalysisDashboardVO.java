package com.smart.campus.web.entity.vo.analysis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LearningAnalysisDashboardVO implements Serializable {

    private String startDate;
    private String endDate;
    private String trendDataRemark;
    private String knowledgeDataRemark;
    private LearningAnalysisOverviewVO overview;
    private LearningAnalysisBehaviorVO behavior;
    private LearningAnalysisReportVO report;
    private List<LearningAnalysisTrendItemVO> trendList = new ArrayList<>();
    private List<LearningAnalysisTrendItemVO> dailyStudyList = new ArrayList<>();
    private List<LearningAnalysisCourseItemVO> courseList = new ArrayList<>();
    private List<LearningAnalysisCourseItemVO> courseDistributionList = new ArrayList<>();
    private List<LearningAnalysisKnowledgeItemVO> knowledgeList = new ArrayList<>();
    private List<LearningAnalysisTimePreferenceItemVO> timePreferenceList = new ArrayList<>();

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getTrendDataRemark() {
        return trendDataRemark;
    }

    public void setTrendDataRemark(String trendDataRemark) {
        this.trendDataRemark = trendDataRemark;
    }

    public String getKnowledgeDataRemark() {
        return knowledgeDataRemark;
    }

    public void setKnowledgeDataRemark(String knowledgeDataRemark) {
        this.knowledgeDataRemark = knowledgeDataRemark;
    }

    public LearningAnalysisOverviewVO getOverview() {
        return overview;
    }

    public void setOverview(LearningAnalysisOverviewVO overview) {
        this.overview = overview;
    }

    public LearningAnalysisBehaviorVO getBehavior() {
        return behavior;
    }

    public void setBehavior(LearningAnalysisBehaviorVO behavior) {
        this.behavior = behavior;
    }

    public LearningAnalysisReportVO getReport() {
        return report;
    }

    public void setReport(LearningAnalysisReportVO report) {
        this.report = report;
    }

    public List<LearningAnalysisTrendItemVO> getTrendList() {
        return trendList;
    }

    public void setTrendList(List<LearningAnalysisTrendItemVO> trendList) {
        this.trendList = trendList;
    }

    public List<LearningAnalysisTrendItemVO> getDailyStudyList() {
        return dailyStudyList;
    }

    public void setDailyStudyList(List<LearningAnalysisTrendItemVO> dailyStudyList) {
        this.dailyStudyList = dailyStudyList;
    }

    public List<LearningAnalysisCourseItemVO> getCourseList() {
        return courseList;
    }

    public void setCourseList(List<LearningAnalysisCourseItemVO> courseList) {
        this.courseList = courseList;
    }

    public List<LearningAnalysisCourseItemVO> getCourseDistributionList() {
        return courseDistributionList;
    }

    public void setCourseDistributionList(List<LearningAnalysisCourseItemVO> courseDistributionList) {
        this.courseDistributionList = courseDistributionList;
    }

    public List<LearningAnalysisKnowledgeItemVO> getKnowledgeList() {
        return knowledgeList;
    }

    public void setKnowledgeList(List<LearningAnalysisKnowledgeItemVO> knowledgeList) {
        this.knowledgeList = knowledgeList;
    }

    public List<LearningAnalysisTimePreferenceItemVO> getTimePreferenceList() {
        return timePreferenceList;
    }

    public void setTimePreferenceList(List<LearningAnalysisTimePreferenceItemVO> timePreferenceList) {
        this.timePreferenceList = timePreferenceList;
    }
}
