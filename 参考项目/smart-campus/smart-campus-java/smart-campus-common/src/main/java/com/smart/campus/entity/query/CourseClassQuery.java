package com.smart.campus.entity.query;



/**
 * 课程班级关联表参数
 */
public class CourseClassQuery extends BaseParam {


	/**
	 * 课程ID
	 */
	private String courseId;

	private String courseIdFuzzy;

	/**
	 * 班级ID
	 */
	private Integer classId;


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

	public void setClassId(Integer classId){
		this.classId = classId;
	}

	public Integer getClassId(){
		return this.classId;
	}

}
