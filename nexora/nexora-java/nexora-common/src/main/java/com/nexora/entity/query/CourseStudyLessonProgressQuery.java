package com.nexora.entity.query;

import java.util.Date;


/**
 * 课时学习进度表参数
 */
public class CourseStudyLessonProgressQuery extends BaseParam {


	/**
	 * 主键
	 */
	private Integer id;

	/**
	 * 学生
	 */
	private Integer userId;

	/**
	 * 课程【冗余】
	 */
	private String courseId;

	private String courseIdFuzzy;

	/**
	 * 课时
	 */
	private String lessonId;

	private String lessonIdFuzzy;

	/**
	 * 视频最后播放位置（秒），续播锚点
	 */
	private Integer playPosition;

	/**
	 * 学习时长（秒）
	 */
	private Integer studyDuration;

	/**
	 * 0未完成 1已完成
	 */
	private Integer finished;

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

	public void setPlayPosition(Integer playPosition){
		this.playPosition = playPosition;
	}

	public Integer getPlayPosition(){
		return this.playPosition;
	}

	public void setStudyDuration(Integer studyDuration){
		this.studyDuration = studyDuration;
	}

	public Integer getStudyDuration(){
		return this.studyDuration;
	}

	public void setFinished(Integer finished){
		this.finished = finished;
	}

	public Integer getFinished(){
		return this.finished;
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
