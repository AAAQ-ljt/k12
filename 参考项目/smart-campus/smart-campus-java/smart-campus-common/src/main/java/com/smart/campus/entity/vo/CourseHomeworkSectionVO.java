package com.smart.campus.entity.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CourseHomeworkSectionVO implements Serializable {

    private Integer sectionId;

    private String sectionName;

    private Integer sortOrder;

    private BigDecimal totalScore;

    private List<CourseHomeworkQuestionVO> questionList = new ArrayList<>();

    public Integer getSectionId() {
        return sectionId;
    }

    public void setSectionId(Integer sectionId) {
        this.sectionId = sectionId;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public BigDecimal getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(BigDecimal totalScore) {
        this.totalScore = totalScore;
    }

    public List<CourseHomeworkQuestionVO> getQuestionList() {
        return questionList;
    }

    public void setQuestionList(List<CourseHomeworkQuestionVO> questionList) {
        this.questionList = questionList;
    }
}
