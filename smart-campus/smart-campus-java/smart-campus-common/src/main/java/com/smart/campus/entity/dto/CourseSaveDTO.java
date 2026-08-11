package com.smart.campus.entity.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CourseSaveDTO implements Serializable {

    public interface Create {}

    public interface Update extends Create {}

    @NotBlank(message = "课程ID不能为空", groups = Update.class)
    private String courseId;

    @NotBlank(message = "课程名称不能为空", groups = {Create.class, Update.class})
    private String courseName;

    private Integer coverResourceId;

    private Integer teacherId;

    private String description;

    private Integer recordStatus;

    private Integer status;

    private List<Integer> classIdList = new ArrayList<>();

    @Valid
    private List<CourseChapterSaveDTO> chapterList = new ArrayList<>();

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

    public Integer getCoverResourceId() {
        return coverResourceId;
    }

    public void setCoverResourceId(Integer coverResourceId) {
        this.coverResourceId = coverResourceId;
    }

    public Integer getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Integer teacherId) {
        this.teacherId = teacherId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getRecordStatus() {
        return recordStatus;
    }

    public void setRecordStatus(Integer recordStatus) {
        this.recordStatus = recordStatus;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public List<Integer> getClassIdList() {
        return classIdList;
    }

    public void setClassIdList(List<Integer> classIdList) {
        this.classIdList = classIdList;
    }

    public List<CourseChapterSaveDTO> getChapterList() {
        return chapterList;
    }

    public void setChapterList(List<CourseChapterSaveDTO> chapterList) {
        this.chapterList = chapterList;
    }
}
