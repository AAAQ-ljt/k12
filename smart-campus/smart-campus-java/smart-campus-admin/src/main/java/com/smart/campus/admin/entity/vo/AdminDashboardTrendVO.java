package com.smart.campus.admin.entity.vo;

public class AdminDashboardTrendVO {

    private String day;
    private String label;
    private Integer course = 0;
    private Integer exam = 0;
    private Integer homework = 0;

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getCourse() {
        return course;
    }

    public void setCourse(Integer course) {
        this.course = course;
    }

    public Integer getExam() {
        return exam;
    }

    public void setExam(Integer exam) {
        this.exam = exam;
    }

    public Integer getHomework() {
        return homework;
    }

    public void setHomework(Integer homework) {
        this.homework = homework;
    }
}
