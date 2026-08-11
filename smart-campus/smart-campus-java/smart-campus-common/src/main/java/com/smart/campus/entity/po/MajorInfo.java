package com.smart.campus.entity.po;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public class MajorInfo implements Serializable {

    public interface Create {}

    public interface Update extends Create {}

    @NotNull(message = "专业ID不能为空", groups = Update.class)
    private Integer majorId;

    @NotNull(message = "所属院系不能为空", groups = {Create.class, Update.class})
    private Integer departmentId;

    @NotBlank(message = "专业编码不能为空", groups = {Create.class, Update.class})
    private String majorCode;

    @NotBlank(message = "专业名称不能为空", groups = {Create.class, Update.class})
    private String majorName;

    @NotNull(message = "学制不能为空", groups = {Create.class, Update.class})
    private Integer educationalSystemType;

    private String description;

    private Integer status;

    private Integer sortOrder;

    public void setMajorId(Integer majorId) {
        this.majorId = majorId;
    }

    public Integer getMajorId() {
        return this.majorId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public Integer getDepartmentId() {
        return this.departmentId;
    }

    public void setMajorCode(String majorCode) {
        this.majorCode = majorCode;
    }

    public String getMajorCode() {
        return this.majorCode;
    }

    public void setMajorName(String majorName) {
        this.majorName = majorName;
    }

    public String getMajorName() {
        return this.majorName;
    }

    public void setEducationalSystemType(Integer educationalSystemType) {
        this.educationalSystemType = educationalSystemType;
    }

    public Integer getEducationalSystemType() {
        return this.educationalSystemType;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getSortOrder() {
        return this.sortOrder;
    }

    @Override
    public String toString() {
        return "主键ID:" + (majorId == null ? "空" : majorId)
                + "，所属院系ID:" + (departmentId == null ? "空" : departmentId)
                + "，专业编码:" + (majorCode == null ? "空" : majorCode)
                + "，专业名称:" + (majorName == null ? "空" : majorName)
                + "，学制:" + (educationalSystemType == null ? "空" : educationalSystemType)
                + "，专业简介:" + (description == null ? "空" : description)
                + "，状态:" + (status == null ? "空" : status)
                + "，排序值:" + (sortOrder == null ? "空" : sortOrder);
    }
}
