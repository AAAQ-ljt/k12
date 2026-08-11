package com.smart.campus.entity.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CourseChapterDetailVO implements Serializable {

    private String chapterId;

    private String courseId;

    private String chapterName;

    private String description;

    private Integer sortOrder;

    private List<CourseLessonDetailVO> lessonList = new ArrayList<>();

    public String getChapterId() {
        return chapterId;
    }

    public void setChapterId(String chapterId) {
        this.chapterId = chapterId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
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

    public List<CourseLessonDetailVO> getLessonList() {
        return lessonList;
    }

    public void setLessonList(List<CourseLessonDetailVO> lessonList) {
        this.lessonList = lessonList;
    }
}
