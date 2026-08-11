package com.smart.campus.entity.query;



/**
 * 学生学习计划主表参数
 */
public class StudyPlanQuery extends BaseParam {


	/**
	 * 学习计划ID
	 */
	private String planId;

	private String planIdFuzzy;

	/**
	 * 课程ID
	 */
	private String courseId;

	private String courseIdFuzzy;

	/**
	 * 学生ID
	 */
	private Integer studentId;

	/**
	 * 0未开始 1进行中 2完成
	 */
	private Integer status;

	/**
	 * 计划说明
	 */
	private String description;

	private String descriptionFuzzy;


	public void setPlanId(String planId){
		this.planId = planId;
	}

	public String getPlanId(){
		return this.planId;
	}

	public void setPlanIdFuzzy(String planIdFuzzy){
		this.planIdFuzzy = planIdFuzzy;
	}

	public String getPlanIdFuzzy(){
		return this.planIdFuzzy;
	}

	public void setCourseId(String courseId){
		this.courseId = courseId;
	}

	public String getCourseId(){
		return this.courseId;
	}

	public void setCourseIdFuzzy(String courseIdFuzzy){
		this.courseIdFuzzy = courseIdFuzzy;
	}

	public String getCourseIdFuzzy(){
		return this.courseIdFuzzy;
	}

	public void setStudentId(Integer studentId){
		this.studentId = studentId;
	}

	public Integer getStudentId(){
		return this.studentId;
	}

	public void setStatus(Integer status){
		this.status = status;
	}

	public Integer getStatus(){
		return this.status;
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

}
