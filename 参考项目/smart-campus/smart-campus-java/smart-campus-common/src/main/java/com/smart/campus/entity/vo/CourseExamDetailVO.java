package com.smart.campus.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CourseExamDetailVO implements Serializable {

    private String examId;
    private String examName;
    private String courseId;
    private String courseName;
    private String paperId;
    private String paperName;
    private Integer remainingSeconds;
    private BigDecimal totalScore;
    private Integer questionCount;
    private Integer answeredCount;
    private Long submitId;
    private Integer submitStatus;
    private String submitStatusText;
    private Integer judgeStatus;
    private String judgeStatusText;
    private Integer usedSeconds;
    private String submitContent;
    private String teacherComment;
    private Boolean editable;
    private Boolean started;
    private Boolean submitted;
    private String examStatusText;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startedTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;

    private List<CourseHomeworkSectionVO> sectionList = new ArrayList<>();

    public String getExamId() {
        return examId;
    }

    public void setExamId(String examId) {
        this.examId = examId;
    }

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
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

    public String getPaperId() {
        return paperId;
    }

    public void setPaperId(String paperId) {
        this.paperId = paperId;
    }

    public String getPaperName() {
        return paperName;
    }

    public void setPaperName(String paperName) {
        this.paperName = paperName;
    }

    public Integer getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(Integer remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }

    public BigDecimal getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(BigDecimal totalScore) {
        this.totalScore = totalScore;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }

    public Integer getAnsweredCount() {
        return answeredCount;
    }

    public void setAnsweredCount(Integer answeredCount) {
        this.answeredCount = answeredCount;
    }

    public Long getSubmitId() {
        return submitId;
    }

    public void setSubmitId(Long submitId) {
        this.submitId = submitId;
    }

    public Integer getSubmitStatus() {
        return submitStatus;
    }

    public void setSubmitStatus(Integer submitStatus) {
        this.submitStatus = submitStatus;
    }

    public String getSubmitStatusText() {
        return submitStatusText;
    }

    public void setSubmitStatusText(String submitStatusText) {
        this.submitStatusText = submitStatusText;
    }

    public Integer getJudgeStatus() {
        return judgeStatus;
    }

    public void setJudgeStatus(Integer judgeStatus) {
        this.judgeStatus = judgeStatus;
    }

    public String getJudgeStatusText() {
        return judgeStatusText;
    }

    public void setJudgeStatusText(String judgeStatusText) {
        this.judgeStatusText = judgeStatusText;
    }

    public Integer getUsedSeconds() {
        return usedSeconds;
    }

    public void setUsedSeconds(Integer usedSeconds) {
        this.usedSeconds = usedSeconds;
    }

    public String getSubmitContent() {
        return submitContent;
    }

    public void setSubmitContent(String submitContent) {
        this.submitContent = submitContent;
    }

    public String getTeacherComment() {
        return teacherComment;
    }

    public void setTeacherComment(String teacherComment) {
        this.teacherComment = teacherComment;
    }

    public Boolean getEditable() {
        return editable;
    }

    public void setEditable(Boolean editable) {
        this.editable = editable;
    }

    public Boolean getStarted() {
        return started;
    }

    public void setStarted(Boolean started) {
        this.started = started;
    }

    public Boolean getSubmitted() {
        return submitted;
    }

    public void setSubmitted(Boolean submitted) {
        this.submitted = submitted;
    }

    public String getExamStatusText() {
        return examStatusText;
    }

    public void setExamStatusText(String examStatusText) {
        this.examStatusText = examStatusText;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getStartedTime() {
        return startedTime;
    }

    public void setStartedTime(Date startedTime) {
        this.startedTime = startedTime;
    }

    public Date getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
    }

    public List<CourseHomeworkSectionVO> getSectionList() {
        return sectionList;
    }

    public void setSectionList(List<CourseHomeworkSectionVO> sectionList) {
        this.sectionList = sectionList;
    }
}
