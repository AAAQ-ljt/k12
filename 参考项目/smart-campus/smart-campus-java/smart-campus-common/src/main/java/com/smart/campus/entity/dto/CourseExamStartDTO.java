package com.smart.campus.entity.dto;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

public class CourseExamStartDTO implements Serializable {

    @NotBlank(message = "考试ID不能为空")
    private String examId;

    public String getExamId() {
        return examId;
    }

    public void setExamId(String examId) {
        this.examId = examId;
    }
}
