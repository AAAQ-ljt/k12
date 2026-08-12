package com.nexora.entity.query;

import java.util.Date;


/**
 * 课程学习进度表参数
 */
public class CourseStudyProgressQuery extends BaseParam {


	/**
	 * 主键
	 */
	private Integer id;

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
	 * 已学课时数【冗余：Redis缓冲聚合后异步回写】
	 */
	private Integer studiedLessons;

	/**
	 * 课时总数【冗余快照】
	 */
	private Integer totalLessons;

	/**
	 * 进度百分比【冗余：列表直读】
	 */
	private Integer progress;

	/**
	 * 累计学习时长（秒）
	 */
	private Integer studyDuration;

	/**
	 * 最近学习课时
	 */
	private String lastLessonId;

	private String lastLessonIdFuzzy;

	/**
	 * 完成时间
	 */
	private String finishTime;

	private String finishTimeStart;

	private String finishTimeEnd;

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


	public void setId(Integer id){
		this.id = id;
	}

	public Integer getId(){
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

	public void setStudiedLessons(Integer studiedLessons){
		this.studiedLessons = studiedLessons;
	}

	public Integer getStudiedLessons(){
		return this.studiedLessons;
	}

	public void setTotalLessons(Integer totalLessons){
		this.totalLessons = totalLessons;
	}

	public Integer getTotalLessons(){
		return this.totalLessons;
	}

	public void setProgress(Integer progress){
		this.progress = progress;
	}

	public Integer getProgress(){
		return this.progress;
	}

	public void setStudyDuration(Integer studyDuration){
		this.studyDuration = studyDuration;
	}

	public Integer getStudyDuration(){
		return this.studyDuration;
	}

	public void setLastLessonId(String lastLessonId){
		this.lastLessonId = lastLessonId;
	}

	public String getLastLessonId(){
		return this.lastLessonId;
	}

	public void setLastLessonIdFuzzy(String lastLessonIdFuzzy){
		this.lastLessonIdFuzzy = lastLessonIdFuzzy;
	}

	public String getLastLessonIdFuzzy(){
		return this.lastLessonIdFuzzy;
	}

	public void setFinishTime(String finishTime){
		this.finishTime = finishTime;
	}

	public String getFinishTime(){
		return this.finishTime;
	}

	public void setFinishTimeStart(String finishTimeStart){
		this.finishTimeStart = finishTimeStart;
	}

	public String getFinishTimeStart(){
		return this.finishTimeStart;
	}
	public void setFinishTimeEnd(String finishTimeEnd){
		this.finishTimeEnd = finishTimeEnd;
	}

	public String getFinishTimeEnd(){
		return this.finishTimeEnd;
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
