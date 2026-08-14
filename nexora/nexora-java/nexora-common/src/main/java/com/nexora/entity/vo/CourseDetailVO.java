package com.nexora.entity.vo;

import com.nexora.entity.po.CourseInfo;

import java.io.Serializable;
import java.util.List;

/**
 * 课程详情：课程 + 章节 + 课时 + 资源
 */
public class CourseDetailVO implements Serializable {

    private CourseInfo course;

    private List<CourseChapterDetailVO> chapters;

    public CourseInfo getCourse() {
        return course;
    }

    public void setCourse(CourseInfo course) {
        this.course = course;
    }

    public List<CourseChapterDetailVO> getChapters() {
        return chapters;
    }

    public void setChapters(List<CourseChapterDetailVO> chapters) {
        this.chapters = chapters;
    }
}
