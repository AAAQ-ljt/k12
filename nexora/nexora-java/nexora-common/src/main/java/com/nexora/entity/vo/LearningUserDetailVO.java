package com.nexora.entity.vo;

import com.nexora.entity.po.UserInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 学习分析-用户个人学习情况详情
 */
public class LearningUserDetailVO {

    private UserInfo userInfo;
    private Long courseCount;
    private Long courseFinishedCount;
    private Integer courseAvgProgress;
    private Long courseStudyDuration;
    private Long practiceCount;
    private Long practiceCorrectCount;
    private Double practiceAccuracy;
    private Long practiceTotalScore;
    private Long wikiResourceCount;
    private Long wikiResourceBytes;
    private Double wikiResourceUsedMb;
    private Double wikiQuotaPercent;
    private Long aiSessionCount;
    private Long aiMessageCount;
    private Long aiTokenCount;
    private Long aiAverageTokens;
    private Integer masteryAvgScore;
    private Integer masteryMasteredCount;
    private Integer masteryInProgressCount;
    private Integer masteryLockedCount;
    private List<CourseStudyProgressItemVO> courseList = new ArrayList<>();
    private List<PracticeKnowledgePointVO> practiceKnowledgePoints = new ArrayList<>();
    private List<PracticeQuestionTypeVO> practiceQuestionTypes = new ArrayList<>();
    private List<KnowledgeResourceVO> knowledgeResources = new ArrayList<>();
    private List<KnowledgeResourceVO> knowledgeResourceTypes = new ArrayList<>();
    private List<AiIntentVO> aiIntents = new ArrayList<>();
    private List<AiRecentMessageVO> aiRecentMessages = new ArrayList<>();
    private List<KnowledgeMasteryVO> masteryList = new ArrayList<>();

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public Long getCourseCount() {
        return courseCount;
    }

    public void setCourseCount(Long courseCount) {
        this.courseCount = courseCount;
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

    public Long getCourseStudyDuration() {
        return courseStudyDuration;
    }

    public void setCourseStudyDuration(Long courseStudyDuration) {
        this.courseStudyDuration = courseStudyDuration;
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

    public Long getPracticeTotalScore() {
        return practiceTotalScore;
    }

    public void setPracticeTotalScore(Long practiceTotalScore) {
        this.practiceTotalScore = practiceTotalScore;
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

    public Double getWikiResourceUsedMb() {
        return wikiResourceUsedMb;
    }

    public void setWikiResourceUsedMb(Double wikiResourceUsedMb) {
        this.wikiResourceUsedMb = wikiResourceUsedMb;
    }

    public Double getWikiQuotaPercent() {
        return wikiQuotaPercent;
    }

    public void setWikiQuotaPercent(Double wikiQuotaPercent) {
        this.wikiQuotaPercent = wikiQuotaPercent;
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

    public Long getAiAverageTokens() {
        return aiAverageTokens;
    }

    public void setAiAverageTokens(Long aiAverageTokens) {
        this.aiAverageTokens = aiAverageTokens;
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

    public Integer getMasteryInProgressCount() {
        return masteryInProgressCount;
    }

    public void setMasteryInProgressCount(Integer masteryInProgressCount) {
        this.masteryInProgressCount = masteryInProgressCount;
    }

    public Integer getMasteryLockedCount() {
        return masteryLockedCount;
    }

    public void setMasteryLockedCount(Integer masteryLockedCount) {
        this.masteryLockedCount = masteryLockedCount;
    }

    public List<CourseStudyProgressItemVO> getCourseList() {
        return courseList;
    }

    public void setCourseList(List<CourseStudyProgressItemVO> courseList) {
        this.courseList = courseList;
    }

    public List<PracticeKnowledgePointVO> getPracticeKnowledgePoints() {
        return practiceKnowledgePoints;
    }

    public void setPracticeKnowledgePoints(List<PracticeKnowledgePointVO> practiceKnowledgePoints) {
        this.practiceKnowledgePoints = practiceKnowledgePoints;
    }

    public List<PracticeQuestionTypeVO> getPracticeQuestionTypes() {
        return practiceQuestionTypes;
    }

    public void setPracticeQuestionTypes(List<PracticeQuestionTypeVO> practiceQuestionTypes) {
        this.practiceQuestionTypes = practiceQuestionTypes;
    }

    public List<KnowledgeResourceVO> getKnowledgeResources() {
        return knowledgeResources;
    }

    public void setKnowledgeResources(List<KnowledgeResourceVO> knowledgeResources) {
        this.knowledgeResources = knowledgeResources;
    }

    public List<KnowledgeResourceVO> getKnowledgeResourceTypes() {
        return knowledgeResourceTypes;
    }

    public void setKnowledgeResourceTypes(List<KnowledgeResourceVO> knowledgeResourceTypes) {
        this.knowledgeResourceTypes = knowledgeResourceTypes;
    }

    public List<AiIntentVO> getAiIntents() {
        return aiIntents;
    }

    public void setAiIntents(List<AiIntentVO> aiIntents) {
        this.aiIntents = aiIntents;
    }

    public List<AiRecentMessageVO> getAiRecentMessages() {
        return aiRecentMessages;
    }

    public void setAiRecentMessages(List<AiRecentMessageVO> aiRecentMessages) {
        this.aiRecentMessages = aiRecentMessages;
    }

    public List<KnowledgeMasteryVO> getMasteryList() {
        return masteryList;
    }

    public void setMasteryList(List<KnowledgeMasteryVO> masteryList) {
        this.masteryList = masteryList;
    }
}
