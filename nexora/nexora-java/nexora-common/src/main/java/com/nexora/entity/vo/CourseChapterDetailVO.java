package com.nexora.entity.vo;

import com.nexora.entity.po.CourseChapter;

import java.io.Serializable;
import java.util.List;

/**
 * 课程章节详情：章节 + 课时列表
 */
public class CourseChapterDetailVO implements Serializable {

    private CourseChapter chapter;

    private List<CourseLessonDetailVO> lessons;

    public CourseChapter getChapter() {
        return chapter;
    }

    public void setChapter(CourseChapter chapter) {
        this.chapter = chapter;
    }

    public List<CourseLessonDetailVO> getLessons() {
        return lessons;
    }

    public void setLessons(List<CourseLessonDetailVO> lessons) {
        this.lessons = lessons;
    }
}
