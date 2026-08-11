package com.smart.campus.entity.query;

import java.util.Date;


/**
 * 在线考试表参数
 */
public class ExamInfoQuery extends BaseParam {


	/**
	 * 考试ID
	 */
	private String examId;

	private String examIdFuzzy;

	/**
	 * 考试名称
	 */
	private String examName;

	private String examNameFuzzy;

	/**
	 * 课程ID
	 */
	private String courseId;

	private String courseIdFuzzy;

	/**
	 * 试卷ID
	 */
	private String paperId;

	private String paperIdFuzzy;

	/**
	 * 教师ID
	 */
	private Integer teacherId;

	/**
	 * 开始时间
	 */
	private String startTime;

	private String startTimeStart;

	private String startTimeEnd;

	/**
	 * 结束时间
	 */
	private String endTime;

	private String endTimeStart;

	private String endTimeEnd;

	/**
	 * 0草稿 1已发布
	 */
	private Integer status;

	/**
	 * 考试说明
	 */
	private String description;

	private String descriptionFuzzy;

	/**
	 * 
	 */
	private String createTime;

	private String createTimeStart;

	private String createTimeEnd;

	/**
	 * 
	 */
	private String updateTime;

	private String updateTimeStart;

	private String updateTimeEnd;


	public void setExamId(String examId){
		this.examId = examId;
	}

	public String getExamId(){
		return this.examId;
	}

	public void setExamIdFuzzy(String examIdFuzzy){
		this.examIdFuzzy = examIdFuzzy;
	}

	public String getExamIdFuzzy(){
		return this.examIdFuzzy;
	}

	public void setExamName(String examName){
		this.examName = examName;
	}

	public String getExamName(){
		return this.examName;
	}

	public void setExamNameFuzzy(String examNameFuzzy){
		this.examNameFuzzy = examNameFuzzy;
	}

	public String getExamNameFuzzy(){
		return this.examNameFuzzy;
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

	public void setPaperId(String paperId){
		this.paperId = paperId;
	}

	public String getPaperId(){
		return this.paperId;
	}

	public void setPaperIdFuzzy(String paperIdFuzzy){
		this.paperIdFuzzy = paperIdFuzzy;
	}

	public String getPaperIdFuzzy(){
		return this.paperIdFuzzy;
	}

	public void setTeacherId(Integer teacherId){
		this.teacherId = teacherId;
	}

	public Integer getTeacherId(){
		return this.teacherId;
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
