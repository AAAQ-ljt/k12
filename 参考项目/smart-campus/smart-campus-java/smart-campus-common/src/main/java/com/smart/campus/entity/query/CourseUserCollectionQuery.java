package com.smart.campus.entity.query;



/**
 * 课程收藏参数
 */
public class CourseUserCollectionQuery extends BaseParam {


	/**
	 * 主键ID
	 */
	private String courseId;

	private String courseIdFuzzy;

	/**
	 * 主键ID
	 */
	private Integer userId;


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

	public void setUserId(Integer userId){
		this.userId = userId;
	}

	public Integer getUserId(){
		return this.userId;
	}

}
