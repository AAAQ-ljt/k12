package com.nexora.entity.vo;

import com.nexora.entity.po.CourseChapterLesson;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 课程课时详情：课时 + 关联资源 + 当前学生的学习完成状态
 */
public class CourseLessonDetailVO implements Serializable {

    private CourseChapterLesson lesson;

    private List<CourseLessonResourceVO> resources;

    /** 是否已配置通关测验 */
    private Boolean quizEnabled;

    /** 当前学生是否已学完该课时（course_study_lesson_progress.finished，打开课时资源即记完成） */
    private Boolean finished;

    /** 该课时的完成时间（未完成时为空） */
    private Date finishTime;

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

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }
}
