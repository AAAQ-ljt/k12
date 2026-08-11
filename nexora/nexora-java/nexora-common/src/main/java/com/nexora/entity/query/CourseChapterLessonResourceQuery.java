package com.nexora.entity.query;

import java.util.Date;


/**
 * 课时资源关联表参数
 */
public class CourseChapterLessonResourceQuery extends BaseParam {


	/**
	 * 主键
	 */
	private Integer id;

	/**
	 * 课时ID
	 */
	private String lessonId;

	private String lessonIdFuzzy;

	/**
	 * 课程ID【冗余】
	 */
	private String courseId;

	private String courseIdFuzzy;

	/**
	 * 资源ID
	 */
	private String resourceId;

	private String resourceIdFuzzy;

	/**
	 * 排序
	 */
	private Integer sort;

	/**
	 * 创建时间
	 */
	private String createTime;

	private String createTimeStart;

	private String createTimeEnd;


	public void setId(Integer id){
		this.id = id;
	}

	public Integer getId(){
		return this.id;
	}

	public void setLessonId(String lessonId){
		this.lessonId = lessonId;
	}

	public String getLessonId(){
		return this.lessonId;
	}

	public void setLessonIdFuzzy(String lessonIdFuzzy){
		this.lessonIdFuzzy = lessonIdFuzzy;
	}

	public String getLessonIdFuzzy(){
		return this.lessonIdFuzzy;
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

	public void setResourceId(String resourceId){
		this.resourceId = resourceId;
	}

	public String getResourceId(){
		return this.resourceId;
	}

	public void setResourceIdFuzzy(String resourceIdFuzzy){
		this.resourceIdFuzzy = resourceIdFuzzy;
	}

	public String getResourceIdFuzzy(){
		return this.resourceIdFuzzy;
	}

	public void setSort(Integer sort){
		this.sort = sort;
	}

	public Integer getSort(){
		return this.sort;
	}

	public void setCreateTime(String createTime){
		this.createTime = createTime;
	}

	public String getCreateTime(){
		return this.createTime;
	}

	public void setCreateTimeStart(String createTimeStart){
		this.createTimeStart = createTimeStart;
	}

	public String getCreateTimeStart(){
		return this.createTimeStart;
	}
	public void setCreateTimeEnd(String createTimeEnd){
		this.createTimeEnd = createTimeEnd;
	}

	public String getCreateTimeEnd(){
		return this.createTimeEnd;
	}

}
