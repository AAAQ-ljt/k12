package com.smart.campus.web.entity.vo.analysis;

import java.io.Serializable;

public class LearningAnalysisKnowledgeItemVO implements Serializable {

    private String key;
    private String courseId;
    private String courseName;
    private String chapterId;
    private String name;
    private Integer mastery;
    private String levelText;
    private String levelTheme;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

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

    public String getChapterId() {
        return chapterId;
    }

    public void setChapterId(String chapterId) {
        this.chapterId = chapterId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMastery() {
        return mastery;
    }

    public void setMastery(Integer mastery) {
        this.mastery = mastery;
    }

    public String getLevelText() {
        return levelText;
    }

    public void setLevelText(String levelText) {
        this.levelText = levelText;
    }

    public String getLevelTheme() {
        return levelTheme;
    }

    public void setLevelTheme(String levelTheme) {
        this.levelTheme = levelTheme;
    }
}
