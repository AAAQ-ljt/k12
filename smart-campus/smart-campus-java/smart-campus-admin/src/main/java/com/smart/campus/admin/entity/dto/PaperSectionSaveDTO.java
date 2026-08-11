package com.smart.campus.admin.entity.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PaperSectionSaveDTO implements Serializable {

    private Integer id;

    @NotBlank(message = "分组名称不能为空")
    private String sectionName;

    private Integer sortOrder;

    @Valid
    private List<PaperQuestionSaveDTO> questionList = new ArrayList<>();

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

    public List<PaperQuestionSaveDTO> getQuestionList() {
        return questionList;
    }

    public void setQuestionList(List<PaperQuestionSaveDTO> questionList) {
        this.questionList = questionList;
    }
}
