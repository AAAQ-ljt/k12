package com.smart.campus.entity.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CourseChapterSaveDTO implements Serializable {

    private String chapterId;

    @NotBlank(message = "章节名称不能为空")
    private String chapterName;

    private String description;

    private Integer sortOrder;

    @Valid
    private List<CourseLessonSaveDTO> lessonList = new ArrayList<>();

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public List<CourseLessonSaveDTO> getLessonList() {
        return lessonList;
    }

    public void setLessonList(List<CourseLessonSaveDTO> lessonList) {
        this.lessonList = lessonList;
    }
}
