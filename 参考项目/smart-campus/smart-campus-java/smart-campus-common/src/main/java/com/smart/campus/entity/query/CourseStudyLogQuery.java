package com.smart.campus.entity.query;

import java.util.Date;


/**
 * 学生学习流水表参数
 */
public class CourseStudyLogQuery extends BaseParam {


	/**
	 * 主键ID
	 */
	private Long id;

	/**
	 * 学习会话ID
	 */
	private String sessionId;

	private String sessionIdFuzzy;

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
	 * 章节ID，对应 course_chapter.chapter_id
	 */
	private String chapterId;

	private String chapterIdFuzzy;

	/**
	 * 课时ID，对应 course_chapter_lesson.lesson_id
	 */
	private String lessonId;

	private String lessonIdFuzzy;

	/**
	 * 本次学习开始时间
	 */
	private String startTime;

	private String startTimeStart;

	private String startTimeEnd;

	/**
	 * 本次学习结束时间
	 */
	private String endTime;

	private String endTimeStart;

	private String endTimeEnd;

	/**
	 * 本次学习时长，单位秒
	 */
	private Integer studySeconds;

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

	public void setSessionId(String sessionId){
		this.sessionId = sessionId;
	}

	public String getSessionId(){
		return this.sessionId;
	}

	public void setSessionIdFuzzy(String sessionIdFuzzy){
		this.sessionIdFuzzy = sessionIdFuzzy;
	}

	public String getSessionIdFuzzy(){
		return this.sessionIdFuzzy;
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

	public void setStartTime(String startTime){
		this.startTime = startTime;
	}

	public String getStartTime(){
		return this.startTime;
	}

	public void setStartTimeStart(String startTimeStart){
		this.startTimeStart = startTimeStart;
	}

	public String getStartTimeStart(){
		return this.startTimeStart;
	}
	public void setStartTimeEnd(String startTimeEnd){
		this.startTimeEnd = startTimeEnd;
	}

	public String getStartTimeEnd(){
		return this.startTimeEnd;
	}

	public void setEndTime(String endTime){
		this.endTime = endTime;
	}

	public String getEndTime(){
		return this.endTime;
	}

	public void setEndTimeStart(String endTimeStart){
		this.endTimeStart = endTimeStart;
	}

	public String getEndTimeStart(){
		return this.endTimeStart;
	}
	public void setEndTimeEnd(String endTimeEnd){
		this.endTimeEnd = endTimeEnd;
	}

	public String getEndTimeEnd(){
		return this.endTimeEnd;
	}

	public void setStudySeconds(Integer studySeconds){
		this.studySeconds = studySeconds;
	}

	public Integer getStudySeconds(){
		return this.studySeconds;
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
