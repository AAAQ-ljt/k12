package com.smart.campus.admin.entity.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PaperSectionVO implements Serializable {

    private Integer id;

    private String sectionName;

    private Integer sortOrder;

    private List<PaperQuestionItemVO> questionList = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public List<PaperQuestionItemVO> getQuestionList() {
        return questionList;
    }

    public void setQuestionList(List<PaperQuestionItemVO> questionList) {
        this.questionList = questionList;
    }
}
