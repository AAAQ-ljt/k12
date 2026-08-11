package com.smart.campus.entity.query;


/**
 * 院系表参数
 */
public class DepartmentInfoQuery extends BaseParam {


    /**
     * 主键ID
     */
    private Integer departmentId;

    /**
     * 院系编码
     */
    private String departmentCode;

    private String departmentCodeFuzzy;

    /**
     * 院系名称
     */
    private String departmentName;

    private String departmentNameFuzzy;

    /**
     * 负责人姓名
     */
    private String leaderName;

    private String leaderNameFuzzy;

    /**
     * 联系电话
     */
    private String contactPhone;

    private String contactPhoneFuzzy;

    /**
     * 院系说明
     */
    private String description;

    private String descriptionFuzzy;

    /**
     * 状态: 1启用 0停用
     */
    private Integer status;

    /**
     * 排序值
     */
    private Integer sortOrder;

    private String keyword;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
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

    public void setDepartmentCodeFuzzy(String departmentCodeFuzzy) {
        this.departmentCodeFuzzy = departmentCodeFuzzy;
    }

    public String getDepartmentCodeFuzzy() {
        return this.departmentCodeFuzzy;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDepartmentName() {
        return this.departmentName;
    }

    public void setDepartmentNameFuzzy(String departmentNameFuzzy) {
        this.departmentNameFuzzy = departmentNameFuzzy;
    }

    public String getDepartmentNameFuzzy() {
        return this.departmentNameFuzzy;
    }

    public void setLeaderName(String leaderName) {
        this.leaderName = leaderName;
    }

    public String getLeaderName() {
        return this.leaderName;
    }

    public void setLeaderNameFuzzy(String leaderNameFuzzy) {
        this.leaderNameFuzzy = leaderNameFuzzy;
    }

    public String getLeaderNameFuzzy() {
        return this.leaderNameFuzzy;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactPhone() {
        return this.contactPhone;
    }

    public void setContactPhoneFuzzy(String contactPhoneFuzzy) {
        this.contactPhoneFuzzy = contactPhoneFuzzy;
    }

    public String getContactPhoneFuzzy() {
        return this.contactPhoneFuzzy;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescriptionFuzzy(String descriptionFuzzy) {
        this.descriptionFuzzy = descriptionFuzzy;
    }

    public String getDescriptionFuzzy() {
        return this.descriptionFuzzy;
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

}
