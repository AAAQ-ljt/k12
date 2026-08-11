package com.smart.campus.entity.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CourseStructureSaveDTO implements Serializable {

    @NotBlank(message = "课程ID不能为空")
    private String courseId;

    @Valid
    private List<CourseChapterSaveDTO> chapterList = new ArrayList<>();

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public List<CourseChapterSaveDTO> getChapterList() {
        return chapterList;
    }

    public void setChapterList(List<CourseChapterSaveDTO> chapterList) {
        this.chapterList = chapterList;
    }
}
