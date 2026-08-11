package com.smart.campus.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CourseHomeworkDetailVO implements Serializable {

    private String courseId;

    private String courseName;

    private String chapterId;

    private String chapterName;

    private String lessonId;

    private String lessonName;

    private String paperId;

    private String paperName;

    private Integer paperType;

    private String paperTypeText;

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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startedTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;

    private List<CourseHomeworkSectionVO> sectionList = new ArrayList<>();

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

    public String getChapterId() {
        return chapterId;
    }

    public void setChapterId(String chapterId) {
        this.chapterId = chapterId;
    }

    public String getChapterName() {
        return chapterName;
    }

    public void setChapterName(String chapterName) {
        this.chapterName = chapterName;
    }

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public String getLessonName() {
        return lessonName;
    }

    public void setLessonName(String lessonName) {
        this.lessonName = lessonName;
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

    public Integer getPaperType() {
        return paperType;
    }

    public void setPaperType(Integer paperType) {
        this.paperType = paperType;
    }

    public String getPaperTypeText() {
        return paperTypeText;
    }

    public void setPaperTypeText(String paperTypeText) {
        this.paperTypeText = paperTypeText;
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
