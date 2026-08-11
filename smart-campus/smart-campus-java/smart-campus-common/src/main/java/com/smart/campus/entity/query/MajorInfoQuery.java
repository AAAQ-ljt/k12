package com.smart.campus.entity.query;



/**
 * 专业表参数
 */
public class MajorInfoQuery extends BaseParam {


	/**
	 * 主键ID
	 */
	private Integer majorId;

	/**
	 * 所属院系ID
	 */
	private Integer departmentId;

	/**
	 * 专业编码
	 */
	private String majorCode;

	private String majorCodeFuzzy;

	/**
	 * 专业名称
	 */
	private String majorName;

	private String majorNameFuzzy;

	/**
	 * 学制，如3年/4年
	 */
	private Integer educationalSystemType;

	/**
	 * 专业简介
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


	public void setMajorId(Integer majorId){
		this.majorId = majorId;
	}

	public Integer getMajorId(){
		return this.majorId;
	}

	public void setDepartmentId(Integer departmentId){
		this.departmentId = departmentId;
	}

	public Integer getDepartmentId(){
		return this.departmentId;
	}

	public void setMajorCode(String majorCode){
		this.majorCode = majorCode;
	}

	public String getMajorCode(){
		return this.majorCode;
	}

	public void setMajorCodeFuzzy(String majorCodeFuzzy){
		this.majorCodeFuzzy = majorCodeFuzzy;
	}

	public String getMajorCodeFuzzy(){
		return this.majorCodeFuzzy;
	}

	public void setMajorName(String majorName){
		this.majorName = majorName;
	}

	public String getMajorName(){
		return this.majorName;
	}

	public void setMajorNameFuzzy(String majorNameFuzzy){
		this.majorNameFuzzy = majorNameFuzzy;
	}

	public String getMajorNameFuzzy(){
		return this.majorNameFuzzy;
	}

	public void setEducationalSystemType(Integer educationalSystemType){
		this.educationalSystemType = educationalSystemType;
	}

	public Integer getEducationalSystemType(){
		return this.educationalSystemType;
	}

	public void setDescription(String description){
		this.description = description;
	}

	public String getDescription(){
		return this.description;
	}

	public void setDescriptionFuzzy(String descriptionFuzzy){
		this.descriptionFuzzy = descriptionFuzzy;
	}

	public String getDescriptionFuzzy(){
		return this.descriptionFuzzy;
	}

	public void setStatus(Integer status){
		this.status = status;
	}

	public Integer getStatus(){
		return this.status;
	}

	public void setSortOrder(Integer sortOrder){
		this.sortOrder = sortOrder;
	}

	public Integer getSortOrder(){
		return this.sortOrder;
	}

}
