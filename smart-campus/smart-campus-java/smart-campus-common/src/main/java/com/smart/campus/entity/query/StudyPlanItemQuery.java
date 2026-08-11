package com.smart.campus.entity.query;

import java.util.Date;


/**
 * 学生学习计划明细表参数
 */
public class StudyPlanItemQuery extends BaseParam {


	/**
	 * 计划明细ID
	 */
	private Long itemId;

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
	 * 章节ID
	 */
	private String chapterId;

	private String chapterIdFuzzy;

	/**
	 * 课时ID
	 */
	private String lessonId;

	private String lessonIdFuzzy;

	/**
	 * 开始时间
	 */
	private Integer startTime;

	/**
	 * 完成日期
	 */
	private String complateTime;

	private String complateTimeStart;

	private String complateTimeEnd;

	/**
	 * 0未开始 1进行中 2完成
	 */
	private Integer status;

	/**
	 * 创建时间
	 */
	private String createTime;

	private String createTimeStart;

	private String createTimeEnd;


	public void setItemId(Long itemId){
		this.itemId = itemId;
	}

	public Long getItemId(){
		return this.itemId;
	}

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

	public void setChapterId(String chapterId){
		this.chapterId = chapterId;
	}

	public String getChapterId(){
		return this.chapterId;
	}

	public void setChapterIdFuzzy(String chapterIdFuzzy){
		this.chapterIdFuzzy = chapterIdFuzzy;
	}

	public String getChapterIdFuzzy(){
		return this.chapterIdFuzzy;
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

	public void setStartTime(Integer startTime){
		this.startTime = startTime;
	}

	public Integer getStartTime(){
		return this.startTime;
	}

	public void setComplateTime(String complateTime){
		this.complateTime = complateTime;
	}

	public String getComplateTime(){
		return this.complateTime;
	}

	public void setComplateTimeStart(String complateTimeStart){
		this.complateTimeStart = complateTimeStart;
	}

	public String getComplateTimeStart(){
		return this.complateTimeStart;
	}
	public void setComplateTimeEnd(String complateTimeEnd){
		this.complateTimeEnd = complateTimeEnd;
	}

	public String getComplateTimeEnd(){
		return this.complateTimeEnd;
	}

	public void setStatus(Integer status){
		this.status = status;
	}

	public Integer getStatus(){
		return this.status;
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
