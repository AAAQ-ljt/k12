package com.nexora.entity.vo;

/**
 * 学习分析-学生列表汇总行
 */
public class LearningUserSummaryVO {

    private String userId;
    private String username;
    private String email;
    private String nickName;
    private String stage;
    private String grade;
    private Integer status;
    private String lastLoginTime;
    private Long courseCourseCount;
    private Long courseFinishedCount;
    private Integer courseAvgProgress;
    private Long practiceCount;
    private Long practiceCorrectCount;
    private Double practiceAccuracy;
    private Long wikiResourceCount;
    private Long wikiResourceBytes;
    private Long aiSessionCount;
    private Long aiMessageCount;
    private Long aiTokenCount;
    private Integer masteryAvgScore;
    private Integer masteryMasteredCount;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(String lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Long getCourseCourseCount() {
        return courseCourseCount;
    }

    public void setCourseCourseCount(Long courseCourseCount) {
        this.courseCourseCount = courseCourseCount;
    }

    public Long getCourseFinishedCount() {
        return courseFinishedCount;
    }

    public void setCourseFinishedCount(Long courseFinishedCount) {
        this.courseFinishedCount = courseFinishedCount;
    }

    public Integer getCourseAvgProgress() {
        return courseAvgProgress;
    }

    public void setCourseAvgProgress(Integer courseAvgProgress) {
        this.courseAvgProgress = courseAvgProgress;
    }

    public Long getPracticeCount() {
        return practiceCount;
    }

    public void setPracticeCount(Long practiceCount) {
        this.practiceCount = practiceCount;
    }

    public Long getPracticeCorrectCount() {
        return practiceCorrectCount;
    }

    public void setPracticeCorrectCount(Long practiceCorrectCount) {
        this.practiceCorrectCount = practiceCorrectCount;
    }

    public Double getPracticeAccuracy() {
        return practiceAccuracy;
    }

    public void setPracticeAccuracy(Double practiceAccuracy) {
        this.practiceAccuracy = practiceAccuracy;
    }

    public Long getWikiResourceCount() {
        return wikiResourceCount;
    }

    public void setWikiResourceCount(Long wikiResourceCount) {
        this.wikiResourceCount = wikiResourceCount;
    }

    public Long getWikiResourceBytes() {
        return wikiResourceBytes;
    }

    public void setWikiResourceBytes(Long wikiResourceBytes) {
        this.wikiResourceBytes = wikiResourceBytes;
    }

    public Long getAiSessionCount() {
        return aiSessionCount;
    }

    public void setAiSessionCount(Long aiSessionCount) {
        this.aiSessionCount = aiSessionCount;
    }

    public Long getAiMessageCount() {
        return aiMessageCount;
    }

    public void setAiMessageCount(Long aiMessageCount) {
        this.aiMessageCount = aiMessageCount;
    }

    public Long getAiTokenCount() {
        return aiTokenCount;
    }

    public void setAiTokenCount(Long aiTokenCount) {
        this.aiTokenCount = aiTokenCount;
    }

    public Integer getMasteryAvgScore() {
        return masteryAvgScore;
    }

    public void setMasteryAvgScore(Integer masteryAvgScore) {
        this.masteryAvgScore = masteryAvgScore;
    }

    public Integer getMasteryMasteredCount() {
        return masteryMasteredCount;
    }

    public void setMasteryMasteredCount(Integer masteryMasteredCount) {
        this.masteryMasteredCount = masteryMasteredCount;
    }
}
