package com.smart.campus.entity.vo;

import java.io.Serializable;
import java.math.BigDecimal;

public class CourseHomeworkSubmitVO implements Serializable {

    private Long submitId;

    private Integer submitStatus;

    private String submitStatusText;

    private Integer judgeStatus;

    private String judgeStatusText;

    private BigDecimal objectiveScore;

    private BigDecimal subjectiveScore;

    private BigDecimal totalScore;

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

    public BigDecimal getObjectiveScore() {
        return objectiveScore;
    }

    public void setObjectiveScore(BigDecimal objectiveScore) {
        this.objectiveScore = objectiveScore;
    }

    public BigDecimal getSubjectiveScore() {
        return subjectiveScore;
    }

    public void setSubjectiveScore(BigDecimal subjectiveScore) {
        this.subjectiveScore = subjectiveScore;
    }

    public BigDecimal getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(BigDecimal totalScore) {
        this.totalScore = totalScore;
    }
}
