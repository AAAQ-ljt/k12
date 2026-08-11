package com.smart.campus.admin.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ExamSaveDTO implements Serializable {

    public interface Create {}

    public interface Update extends Create {}

    @NotBlank(message = "考试ID不能为空", groups = Update.class)
    private String examId;

    @NotBlank(message = "考试名称不能为空", groups = {Create.class, Update.class})
    private String examName;

    @NotBlank(message = "课程不能为空", groups = {Create.class, Update.class})
    private String courseId;

    @NotBlank(message = "试卷不能为空", groups = {Create.class, Update.class})
    private String paperId;

    @NotNull(message = "开始时间不能为空", groups = {Create.class, Update.class})
    private String startTime;

    @NotNull(message = "结束时间不能为空", groups = {Create.class, Update.class})
    private String endTime;

    private Integer status;

    private String description;

    @NotEmpty(message = "请选择考试班级", groups = {Create.class, Update.class})
    private List<Integer> classIdList = new ArrayList<>();

    public String getExamId() {
        return examId;
    }

    public void setExamId(String examId) {
        this.examId = examId;
    }

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getPaperId() {
        return paperId;
    }

    public void setPaperId(String paperId) {
        this.paperId = paperId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Integer> getClassIdList() {
        return classIdList;
    }

    public void setClassIdList(List<Integer> classIdList) {
        this.classIdList = classIdList;
    }
}
