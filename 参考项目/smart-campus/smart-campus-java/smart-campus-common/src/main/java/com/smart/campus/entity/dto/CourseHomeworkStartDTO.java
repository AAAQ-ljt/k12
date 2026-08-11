package com.smart.campus.entity.dto;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

public class CourseHomeworkStartDTO implements Serializable {

    @NotBlank(message = "课程ID不能为空")
    private String courseId;

    @NotBlank(message = "课时ID不能为空")
    private String lessonId;

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }
}
