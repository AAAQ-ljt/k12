package com.smart.campus.entity.query;



/**
 * 班级表参数
 */
public class ClassInfoQuery extends BaseParam {


	/**
	 * 主键ID
	 */
	private Integer classId;

	/**
	 * 所属院系ID
	 */
	private Integer departmentId;

	/**
	 * 所属专业ID
	 */
	private Integer majorId;

	/**
	 * 班级名称
	 */
	private String className;

	private String classNameFuzzy;

	/**
	 * 辅导员姓名
	 */
	private String counselorName;

	private String counselorNameFuzzy;

	/**
	 * 班主任姓名
	 */
	private String headTeacherName;

	private String headTeacherNameFuzzy;

	/**
	 * 班级说明
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


	public void setClassId(Integer classId){
		this.classId = classId;
	}

	public Integer getClassId(){
		return this.classId;
	}

	public void setDepartmentId(Integer departmentId){
		this.departmentId = departmentId;
	}

	public Integer getDepartmentId(){
		return this.departmentId;
	}

	public void setMajorId(Integer majorId){
		this.majorId = majorId;
	}

	public Integer getMajorId(){
		return this.majorId;
	}

	public void setClassName(String className){
		this.className = className;
	}

	public String getClassName(){
		return this.className;
	}

	public void setClassNameFuzzy(String classNameFuzzy){
		this.classNameFuzzy = classNameFuzzy;
	}

	public String getClassNameFuzzy(){
		return this.classNameFuzzy;
	}

	public void setCounselorName(String counselorName){
		this.counselorName = counselorName;
	}

	public String getCounselorName(){
		return this.counselorName;
	}

	public void setCounselorNameFuzzy(String counselorNameFuzzy){
		this.counselorNameFuzzy = counselorNameFuzzy;
	}

	public String getCounselorNameFuzzy(){
		return this.counselorNameFuzzy;
	}

	public void setHeadTeacherName(String headTeacherName){
		this.headTeacherName = headTeacherName;
	}

	public String getHeadTeacherName(){
		return this.headTeacherName;
	}

	public void setHeadTeacherNameFuzzy(String headTeacherNameFuzzy){
		this.headTeacherNameFuzzy = headTeacherNameFuzzy;
	}

	public String getHeadTeacherNameFuzzy(){
		return this.headTeacherNameFuzzy;
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
