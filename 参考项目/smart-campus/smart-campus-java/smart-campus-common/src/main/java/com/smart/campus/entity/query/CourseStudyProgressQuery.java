package com.smart.campus.entity.query;

import java.util.Date;


/**
 * 学生课程学习进度表参数
 */
public class CourseStudyProgressQuery extends BaseParam {


	/**
	 * 主键ID
	 */
	private Long id;

	/**
	 * 学生ID，对应 user_info.user_id
	 */
	private Integer userId;

	/**
	 * 课程ID，对应 course_info.course_id
	 */
	private String courseId;

	private String courseIdFuzzy;

	/**
	 * 当前学习章节ID，对应 course_chapter.chapter_id
	 */
	private String currentChapterId;

	private String currentChapterIdFuzzy;

	/**
	 * 当前学习课时ID，对应 course_chapter_lesson.lesson_id
	 */
	private String currentLessonId;

	private String currentLessonIdFuzzy;

	/**
	 * 累计学习时长，单位秒，重复观看可累计
	 */
	private Integer studySeconds;

	/**
	 * 最后学习时间
	 */
	private String lastStudyTime;

	private String lastStudyTimeStart;

	private String lastStudyTimeEnd;

	/**
	 * 学习状态: 0未开始 1学习中 2已完成
	 */
	private Integer status;

	/**
	 * 创建时间
	 */
	private String createTime;

	private String createTimeStart;

	private String createTimeEnd;

	/**
	 * 更新时间
	 */
	private String updateTime;

	private String updateTimeStart;

	private String updateTimeEnd;


	public void setId(Long id){
		this.id = id;
	}

	public Long getId(){
		return this.id;
	}

	public void setUserId(Integer userId){
		this.userId = userId;
	}

	public Integer getUserId(){
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

	public void setCurrentChapterId(String currentChapterId){
		this.currentChapterId = currentChapterId;
	}

	public String getCurrentChapterId(){
		return this.currentChapterId;
	}

	public void setCurrentChapterIdFuzzy(String currentChapterIdFuzzy){
		this.currentChapterIdFuzzy = currentChapterIdFuzzy;
	}

	public String getCurrentChapterIdFuzzy(){
		return this.currentChapterIdFuzzy;
	}

	public void setCurrentLessonId(String currentLessonId){
		this.currentLessonId = currentLessonId;
	}

	public String getCurrentLessonId(){
		return this.currentLessonId;
	}

	public void setCurrentLessonIdFuzzy(String currentLessonIdFuzzy){
		this.currentLessonIdFuzzy = currentLessonIdFuzzy;
	}

	public String getCurrentLessonIdFuzzy(){
		return this.currentLessonIdFuzzy;
	}

	public void setStudySeconds(Integer studySeconds){
		this.studySeconds = studySeconds;
	}

	public Integer getStudySeconds(){
		return this.studySeconds;
	}

	public void setLastStudyTime(String lastStudyTime){
		this.lastStudyTime = lastStudyTime;
	}

	public String getLastStudyTime(){
		return this.lastStudyTime;
	}

	public void setLastStudyTimeStart(String lastStudyTimeStart){
		this.lastStudyTimeStart = lastStudyTimeStart;
	}

	public String getLastStudyTimeStart(){
		return this.lastStudyTimeStart;
	}
	public void setLastStudyTimeEnd(String lastStudyTimeEnd){
		this.lastStudyTimeEnd = lastStudyTimeEnd;
	}

	public String getLastStudyTimeEnd(){
		return this.lastStudyTimeEnd;
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

	public void setUpdateTime(String updateTime){
		this.updateTime = updateTime;
	}

	public String getUpdateTime(){
		return this.updateTime;
	}

	public void setUpdateTimeStart(String updateTimeStart){
		this.updateTimeStart = updateTimeStart;
	}

	public String getUpdateTimeStart(){
		return this.updateTimeStart;
	}
	public void setUpdateTimeEnd(String updateTimeEnd){
		this.updateTimeEnd = updateTimeEnd;
	}

	public String getUpdateTimeEnd(){
		return this.updateTimeEnd;
	}

}
