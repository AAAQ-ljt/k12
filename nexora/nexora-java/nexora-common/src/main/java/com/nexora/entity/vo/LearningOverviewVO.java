package com.nexora.entity.vo;

/**
 * 学习分析总览
 */
public class LearningOverviewVO {

    private Integer studentCount;
    private Integer courseActiveStudents;
    private Integer courseAvgProgress;
    private Integer courseTotalDuration;
    private Integer practiceTotal;
    private Integer practiceCorrect;
    private Double practiceAccuracy;
    private Integer wikiResourceCount;
    private Integer wikiActiveUsers;
    private Integer aiMessageCount;
    private Integer aiActiveUsers;
    private Integer aiTotalTokens;
    private Integer masteryAvgScore;

    public Integer getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }

    public Integer getCourseActiveStudents() {
        return courseActiveStudents;
    }

    public void setCourseActiveStudents(Integer courseActiveStudents) {
        this.courseActiveStudents = courseActiveStudents;
    }

    public Integer getCourseAvgProgress() {
        return courseAvgProgress;
    }

    public void setCourseAvgProgress(Integer courseAvgProgress) {
        this.courseAvgProgress = courseAvgProgress;
    }

    public Integer getCourseTotalDuration() {
        return courseTotalDuration;
    }

    public void setCourseTotalDuration(Integer courseTotalDuration) {
        this.courseTotalDuration = courseTotalDuration;
    }

    public Integer getPracticeTotal() {
        return practiceTotal;
    }

    public void setPracticeTotal(Integer practiceTotal) {
        this.practiceTotal = practiceTotal;
    }

    public Integer getPracticeCorrect() {
        return practiceCorrect;
    }

    public void setPracticeCorrect(Integer practiceCorrect) {
        this.practiceCorrect = practiceCorrect;
    }

    public Double getPracticeAccuracy() {
        return practiceAccuracy;
    }

    public void setPracticeAccuracy(Double practiceAccuracy) {
        this.practiceAccuracy = practiceAccuracy;
    }

    public Integer getWikiResourceCount() {
        return wikiResourceCount;
    }

    public void setWikiResourceCount(Integer wikiResourceCount) {
        this.wikiResourceCount = wikiResourceCount;
    }

    public Integer getWikiActiveUsers() {
        return wikiActiveUsers;
    }

    public void setWikiActiveUsers(Integer wikiActiveUsers) {
        this.wikiActiveUsers = wikiActiveUsers;
    }

    public Integer getAiMessageCount() {
        return aiMessageCount;
    }

    public void setAiMessageCount(Integer aiMessageCount) {
        this.aiMessageCount = aiMessageCount;
    }

    public Integer getAiActiveUsers() {
        return aiActiveUsers;
    }

    public void setAiActiveUsers(Integer aiActiveUsers) {
        this.aiActiveUsers = aiActiveUsers;
    }

    public Integer getAiTotalTokens() {
        return aiTotalTokens;
    }

    public void setAiTotalTokens(Integer aiTotalTokens) {
        this.aiTotalTokens = aiTotalTokens;
    }

    public Integer getMasteryAvgScore() {
        return masteryAvgScore;
    }

    public void setMasteryAvgScore(Integer masteryAvgScore) {
        this.masteryAvgScore = masteryAvgScore;
    }
}
