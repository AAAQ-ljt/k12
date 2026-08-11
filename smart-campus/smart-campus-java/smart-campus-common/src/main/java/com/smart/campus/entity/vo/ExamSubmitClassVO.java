package com.smart.campus.entity.vo;

import java.io.Serializable;

public class ExamSubmitClassVO implements Serializable {

    private Integer classId;

    private String className;

    private String majorName;

    private String departmentName;

    private Integer studentCount = 0;

    private Integer submittedCount = 0;

    private Integer waitJudgeCount = 0;

    public Integer getClassId() {
        return classId;
    }

    public void setClassId(Integer classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMajorName() {
        return majorName;
    }

    public void setMajorName(String majorName) {
        this.majorName = majorName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Integer getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }

    public Integer getSubmittedCount() {
        return submittedCount;
    }

    public void setSubmittedCount(Integer submittedCount) {
        this.submittedCount = submittedCount;
    }

    public Integer getWaitJudgeCount() {
        return waitJudgeCount;
    }

    public void setWaitJudgeCount(Integer waitJudgeCount) {
        this.waitJudgeCount = waitJudgeCount;
    }
}
