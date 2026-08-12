package com.nexora.entity.query;

import java.util.Date;


/**
 * 学习日志表参数
 */
public class CourseStudyLogQuery extends BaseParam {


	/**
	 * 主键
	 */
	private Long id;

	/**
	 * 学生
	 */
	private String userId;

	/**
	 * 课程
	 */
	private String courseId;

	private String courseIdFuzzy;

	/**
	 * 课时
	 */
	private String lessonId;

	private String lessonIdFuzzy;

	/**
	 * 学习日期【冗余：连续打卡/时长统计按天聚合】
	 */
	private String studyDate;

	private String studyDateStart;

	private String studyDateEnd;

	/**
	 * 本次时长（秒）
	 */
	private Integer duration;

	/**
	 * 创建时间
	 */
	private String createTime;

	private String createTimeStart;

	private String createTimeEnd;


	public void setId(Long id){
		this.id = id;
	}

	public Long getId(){
		return this.id;
	}

	public void setUserId(String userId){
		this.userId = userId;
	}

	public String getUserId(){
		return this.userId;
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

	public void setStudyDate(String studyDate){
		this.studyDate = studyDate;
	}

	public String getStudyDate(){
		return this.studyDate;
	}

	public void setStudyDateStart(String studyDateStart){
		this.studyDateStart = studyDateStart;
	}

	public String getStudyDateStart(){
		return this.studyDateStart;
	}
	public void setStudyDateEnd(String studyDateEnd){
		this.studyDateEnd = studyDateEnd;
	}

	public String getStudyDateEnd(){
		return this.studyDateEnd;
	}

	public void setDuration(Integer duration){
		this.duration = duration;
	}

	public Integer getDuration(){
		return this.duration;
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
