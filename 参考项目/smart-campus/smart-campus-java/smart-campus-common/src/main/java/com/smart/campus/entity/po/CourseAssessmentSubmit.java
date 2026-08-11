package com.smart.campus.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.Date;
import com.smart.campus.entity.enums.DateTimePatternEnum;
import com.smart.campus.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 课程作业/考试学生提交表
 */
public class CourseAssessmentSubmit implements Serializable {


	/**
	 * 主键ID
	 */
	private Long submitId;

	/**
	 * 任务ID，对应course_chapter_lesson.lesson_id或者考试ID
	 */
	private String taskId;

	/**
	 * 任务类型快照: 1作业 2考试
	 */
	private Integer taskType;

	/**
	 * 试卷ID
	 */
	private String paperId;

	/**
	 * 学生ID，对应 user_info.user_id
	 */
	private Integer userId;

	/**
	 * 提交状态:0待开始 1作答中 2草稿 3已提交
	 */
	private Integer submitStatus;

	/**
	 * 批改状态: 0未批改 1自动判分完成 2待人工批改 3人工批改完成
	 */
	private Integer judgeStatus;

	/**
	 * 开始作答时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date startedTime;

	/**
	 * 提交时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date submitTime;

	/**
	 * 强制交卷时间，如超时系统自动提交
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date forcedSubmitTime;

	/**
	 * 本次作答耗时，单位秒
	 */
	private Integer usedSeconds;

	/**
	 * 客观题得分
	 */
	private BigDecimal objectiveScore;

	/**
	 * 主观题得分
	 */
	private BigDecimal subjectiveScore;

	/**
	 * 整卷提交补充内容，建议存JSON，如总体说明、附件等
	 */
	private String submitContent;

	/**
	 * 教师评语
	 */
	private String teacherComment;

	/**
	 * 批改完成时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date judgeTime;

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	/**
	 * 更新时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date updateTime;


	public void setSubmitId(Long submitId){
		this.submitId = submitId;
	}

	public Long getSubmitId(){
		return this.submitId;
	}

	public void setTaskId(String taskId){
		this.taskId = taskId;
	}

	public String getTaskId(){
		return this.taskId;
	}

	public void setTaskType(Integer taskType){
		this.taskType = taskType;
	}

	public Integer getTaskType(){
		return this.taskType;
	}

	public void setPaperId(String paperId){
		this.paperId = paperId;
	}

	public String getPaperId(){
		return this.paperId;
	}

	public void setUserId(Integer userId){
		this.userId = userId;
	}

	public Integer getUserId(){
		return this.userId;
	}

	public void setSubmitStatus(Integer submitStatus){
		this.submitStatus = submitStatus;
	}

	public Integer getSubmitStatus(){
		return this.submitStatus;
	}

	public void setJudgeStatus(Integer judgeStatus){
		this.judgeStatus = judgeStatus;
	}

	public Integer getJudgeStatus(){
		return this.judgeStatus;
	}

	public void setStartedTime(Date startedTime){
		this.startedTime = startedTime;
	}

	public Date getStartedTime(){
		return this.startedTime;
	}

	public void setSubmitTime(Date submitTime){
		this.submitTime = submitTime;
	}

	public Date getSubmitTime(){
		return this.submitTime;
	}

	public void setForcedSubmitTime(Date forcedSubmitTime){
		this.forcedSubmitTime = forcedSubmitTime;
	}

	public Date getForcedSubmitTime(){
		return this.forcedSubmitTime;
	}

	public void setUsedSeconds(Integer usedSeconds){
		this.usedSeconds = usedSeconds;
	}

	public Integer getUsedSeconds(){
		return this.usedSeconds;
	}

	public void setObjectiveScore(BigDecimal objectiveScore){
		this.objectiveScore = objectiveScore;
	}

	public BigDecimal getObjectiveScore(){
		return this.objectiveScore;
	}

	public void setSubjectiveScore(BigDecimal subjectiveScore){
		this.subjectiveScore = subjectiveScore;
	}

	public BigDecimal getSubjectiveScore(){
		return this.subjectiveScore;
	}

	public void setSubmitContent(String submitContent){
		this.submitContent = submitContent;
	}

	public String getSubmitContent(){
		return this.submitContent;
	}

	public void setTeacherComment(String teacherComment){
		this.teacherComment = teacherComment;
	}

	public String getTeacherComment(){
		return this.teacherComment;
	}

	public void setJudgeTime(Date judgeTime){
		this.judgeTime = judgeTime;
	}

	public Date getJudgeTime(){
		return this.judgeTime;
	}

	public void setCreateTime(Date createTime){
		this.createTime = createTime;
	}

	public Date getCreateTime(){
		return this.createTime;
	}

	public void setUpdateTime(Date updateTime){
		this.updateTime = updateTime;
	}

	public Date getUpdateTime(){
		return this.updateTime;
	}

	@Override
	public String toString (){
		return "主键ID:"+(submitId == null ? "空" : submitId)+"，任务ID，对应course_chapter_lesson.lesson_id或者考试ID:"+(taskId == null ? "空" : taskId)+"，任务类型快照: 1作业 2考试:"+(taskType == null ? "空" : taskType)+"，试卷ID:"+(paperId == null ? "空" : paperId)+"，学生ID，对应 user_info.user_id:"+(userId == null ? "空" : userId)+"，提交状态:0待开始 1作答中 2草稿 3已提交:"+(submitStatus == null ? "空" : submitStatus)+"，批改状态: 0未批改 1自动判分完成 2待人工批改 3人工批改完成:"+(judgeStatus == null ? "空" : judgeStatus)+"，开始作答时间:"+(startedTime == null ? "空" : DateUtil.format(startedTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，提交时间:"+(submitTime == null ? "空" : DateUtil.format(submitTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，强制交卷时间，如超时系统自动提交:"+(forcedSubmitTime == null ? "空" : DateUtil.format(forcedSubmitTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，本次作答耗时，单位秒:"+(usedSeconds == null ? "空" : usedSeconds)+"，客观题得分:"+(objectiveScore == null ? "空" : objectiveScore)+"，主观题得分:"+(subjectiveScore == null ? "空" : subjectiveScore)+"，整卷提交补充内容，建议存JSON，如总体说明、附件等:"+(submitContent == null ? "空" : submitContent)+"，教师评语:"+(teacherComment == null ? "空" : teacherComment)+"，批改完成时间:"+(judgeTime == null ? "空" : DateUtil.format(judgeTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，更新时间:"+(updateTime == null ? "空" : DateUtil.format(updateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
