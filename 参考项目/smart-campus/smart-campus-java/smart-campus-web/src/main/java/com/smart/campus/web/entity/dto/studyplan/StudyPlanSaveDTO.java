package com.smart.campus.web.entity.dto.studyplan;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.ArrayList;
import java.util.List;

public class StudyPlanSaveDTO {

    private String planId;

    @NotBlank(message = "课程ID不能为空")
    private String courseId;

    private String description;

    @Valid
    @NotEmpty(message = "请至少选择一个学习章节")
    private List<StudyPlanSaveItemDTO> itemList = new ArrayList<>();

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<StudyPlanSaveItemDTO> getItemList() {
        return itemList;
    }

    public void setItemList(List<StudyPlanSaveItemDTO> itemList) {
        this.itemList = itemList;
    }
}
