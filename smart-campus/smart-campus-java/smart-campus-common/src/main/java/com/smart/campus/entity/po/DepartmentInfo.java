package com.smart.campus.entity.po;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public class DepartmentInfo implements Serializable {

    public interface Create {}

    public interface Update extends Create {}

    @NotNull(message = "院系ID不能为空", groups = Update.class)
    private Integer departmentId;

    @NotBlank(message = "院系编码不能为空", groups = {Create.class, Update.class})
    private String departmentCode;

    @NotBlank(message = "院系名称不能为空", groups = {Create.class, Update.class})
    private String departmentName;

    @NotBlank(message = "负责人不能为空", groups = {Create.class, Update.class})
    private String leaderName;

    private String contactPhone;

    private String description;

    private Integer status;

    private Integer sortOrder;

    private Integer majorCount;

    public Integer getMajorCount() {
        return majorCount;
    }

    public void setMajorCount(Integer majorCount) {
        this.majorCount = majorCount;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public Integer getDepartmentId() {
        return this.departmentId;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public String getDepartmentCode() {
        return this.departmentCode;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDepartmentName() {
        return this.departmentName;
    }

    public void setLeaderName(String leaderName) {
        this.leaderName = leaderName;
    }

    public String getLeaderName() {
        return this.leaderName;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactPhone() {
        return this.contactPhone;
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
        return "主键ID:" + (departmentId == null ? "空" : departmentId)
                + "，院系编码:" + (departmentCode == null ? "空" : departmentCode)
                + "，院系名称:" + (departmentName == null ? "空" : departmentName)
                + "，负责人姓名:" + (leaderName == null ? "空" : leaderName)
                + "，联系电话:" + (contactPhone == null ? "空" : contactPhone)
                + "，院系说明:" + (description == null ? "空" : description)
                + "，状态:" + (status == null ? "空" : status)
                + "，排序值:" + (sortOrder == null ? "空" : sortOrder);
    }
}
