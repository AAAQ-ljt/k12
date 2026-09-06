package com.nexora.entity.vo;

import com.nexora.entity.po.CourseChapterLesson;

import java.io.Serializable;
import java.util.List;

/**
 * 课程课时详情：课时 + 关联资源
 */
public class CourseLessonDetailVO implements Serializable {

    private CourseChapterLesson lesson;

    private List<CourseLessonResourceVO> resources;

    /** 是否已配置通关测验 */
    private Boolean quizEnabled;

    public CourseChapterLesson getLesson() {
        return lesson;
    }

    public void setLesson(CourseChapterLesson lesson) {
        this.lesson = lesson;
    }

    public List<CourseLessonResourceVO> getResources() {
        return resources;
    }

    public void setResources(List<CourseLessonResourceVO> resources) {
        this.resources = resources;
    }

    public Boolean getQuizEnabled() {
        return quizEnabled;
    }

    public void setQuizEnabled(Boolean quizEnabled) {
        this.quizEnabled = quizEnabled;
    }
}
