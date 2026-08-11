package com.smart.campus.entity.vo;

import java.util.List;
import java.util.Map;

public class BasicDataOptionsVO {

    private Map<Integer, String> statusTextMap;

    private Map<Integer, String> genderTextMap;

    private List<OptionVO<Integer>> departmentOptions;

    private List<OptionVO<Integer>> majorOptions;

    private List<OptionVO<Integer>> classOptions;

    private List<OptionVO<Integer>> gradeOptions;

    private List<OptionVO<String>> teacherTitleOptions;

    public Map<Integer, String> getStatusTextMap() {
        return statusTextMap;
    }

    public void setStatusTextMap(Map<Integer, String> statusTextMap) {
        this.statusTextMap = statusTextMap;
    }

    public Map<Integer, String> getGenderTextMap() {
        return genderTextMap;
    }

    public void setGenderTextMap(Map<Integer, String> genderTextMap) {
        this.genderTextMap = genderTextMap;
    }

    public List<OptionVO<Integer>> getDepartmentOptions() {
        return departmentOptions;
    }

    public void setDepartmentOptions(List<OptionVO<Integer>> departmentOptions) {
        this.departmentOptions = departmentOptions;
    }

    public List<OptionVO<Integer>> getMajorOptions() {
        return majorOptions;
    }

    public void setMajorOptions(List<OptionVO<Integer>> majorOptions) {
        this.majorOptions = majorOptions;
    }

    public List<OptionVO<Integer>> getClassOptions() {
        return classOptions;
    }

    public void setClassOptions(List<OptionVO<Integer>> classOptions) {
        this.classOptions = classOptions;
    }

    public List<OptionVO<Integer>> getGradeOptions() {
        return gradeOptions;
    }

    public void setGradeOptions(List<OptionVO<Integer>> gradeOptions) {
        this.gradeOptions = gradeOptions;
    }

    public List<OptionVO<String>> getTeacherTitleOptions() {
        return teacherTitleOptions;
    }

    public void setTeacherTitleOptions(List<OptionVO<String>> teacherTitleOptions) {
        this.teacherTitleOptions = teacherTitleOptions;
    }
}
