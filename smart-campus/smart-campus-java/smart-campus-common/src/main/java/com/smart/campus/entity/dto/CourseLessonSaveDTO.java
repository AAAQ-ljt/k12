package com.smart.campus.entity.dto;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CourseLessonSaveDTO implements Serializable {

    private String lessonId;

    @NotBlank(message = "课时名称不能为空")
    private String lessonName;

    private Integer sortOrder;

    private Integer videoResourceId;

    private List<Integer> coursewareResourceIdList = new ArrayList<>();

    private String paperId;

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

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getVideoResourceId() {
        return videoResourceId;
    }

    public void setVideoResourceId(Integer videoResourceId) {
        this.videoResourceId = videoResourceId;
    }

    public List<Integer> getCoursewareResourceIdList() {
        return coursewareResourceIdList;
    }

    public void setCoursewareResourceIdList(List<Integer> coursewareResourceIdList) {
        this.coursewareResourceIdList = coursewareResourceIdList;
    }

    public String getPaperId() {
        return paperId;
    }

    public void setPaperId(String paperId) {
        this.paperId = paperId;
    }
}
