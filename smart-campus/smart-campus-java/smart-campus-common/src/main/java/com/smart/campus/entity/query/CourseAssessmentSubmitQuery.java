package com.smart.campus.entity.query;

import java.math.BigDecimal;
import java.util.Date;


/**
 * 课程作业/考试学生提交表参数
 */
public class CourseAssessmentSubmitQuery extends BaseParam {


	/**
	 * 主键ID
	 */
	private Long submitId;

	/**
	 * 任务ID，对应course_chapter_lesson.lesson_id或者考试ID
	 */
	private String taskId;

	private String taskIdFuzzy;

	/**
	 * 任务类型快照: 1作业 2考试
	 */
	private Integer taskType;

	/**
	 * 试卷ID
	 */
	private String paperId;

	private String paperIdFuzzy;

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
	private String startedTime;

	private String startedTimeStart;

	private String startedTimeEnd;

	/**
	 * 提交时间
	 */
	private String submitTime;

	private String submitTimeStart;

	private String submitTimeEnd;

	/**
	 * 强制交卷时间，如超时系统自动提交
	 */
	private String forcedSubmitTime;

	private String forcedSubmitTimeStart;

	private String forcedSubmitTimeEnd;

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

	private String submitContentFuzzy;

	/**
	 * 教师评语
	 */
	private String teacherComment;

	private String teacherCommentFuzzy;

	/**
	 * 批改完成时间
	 */
	private String judgeTime;

	private String judgeTimeStart;

	private String judgeTimeEnd;

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

	public void setTaskIdFuzzy(String taskIdFuzzy){
		this.taskIdFuzzy = taskIdFuzzy;
	}

	public String getTaskIdFuzzy(){
		return this.taskIdFuzzy;
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

	public void setPaperIdFuzzy(String paperIdFuzzy){
		this.paperIdFuzzy = paperIdFuzzy;
	}

	public String getPaperIdFuzzy(){
		return this.paperIdFuzzy;
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

	public void setStartedTime(String startedTime){
		this.startedTime = startedTime;
	}

	public String getStartedTime(){
		return this.startedTime;
	}

	public void setStartedTimeStart(String startedTimeStart){
		this.startedTimeStart = startedTimeStart;
	}

	public String getStartedTimeStart(){
		return this.startedTimeStart;
	}
	public void setStartedTimeEnd(String startedTimeEnd){
		this.startedTimeEnd = startedTimeEnd;
	}

	public String getStartedTimeEnd(){
		return this.startedTimeEnd;
	}

	public void setSubmitTime(String submitTime){
		this.submitTime = submitTime;
	}

	public String getSubmitTime(){
		return this.submitTime;
	}

	public void setSubmitTimeStart(String submitTimeStart){
		this.submitTimeStart = submitTimeStart;
	}

	public String getSubmitTimeStart(){
		return this.submitTimeStart;
	}
	public void setSubmitTimeEnd(String submitTimeEnd){
		this.submitTimeEnd = submitTimeEnd;
	}

	public String getSubmitTimeEnd(){
		return this.submitTimeEnd;
	}

	public void setForcedSubmitTime(String forcedSubmitTime){
		this.forcedSubmitTime = forcedSubmitTime;
	}

	public String getForcedSubmitTime(){
		return this.forcedSubmitTime;
	}

	public void setForcedSubmitTimeStart(String forcedSubmitTimeStart){
		this.forcedSubmitTimeStart = forcedSubmitTimeStart;
	}

	public String getForcedSubmitTimeStart(){
		return this.forcedSubmitTimeStart;
	}
	public void setForcedSubmitTimeEnd(String forcedSubmitTimeEnd){
		this.forcedSubmitTimeEnd = forcedSubmitTimeEnd;
	}

	public String getForcedSubmitTimeEnd(){
		return this.forcedSubmitTimeEnd;
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

	public void setSubmitContentFuzzy(String submitContentFuzzy){
		this.submitContentFuzzy = submitContentFuzzy;
	}

	public String getSubmitContentFuzzy(){
		return this.submitContentFuzzy;
	}

	public void setTeacherComment(String teacherComment){
		this.teacherComment = teacherComment;
	}

	public String getTeacherComment(){
		return this.teacherComment;
	}

	public void setTeacherCommentFuzzy(String teacherCommentFuzzy){
		this.teacherCommentFuzzy = teacherCommentFuzzy;
	}

	public String getTeacherCommentFuzzy(){
		return this.teacherCommentFuzzy;
	}

	public void setJudgeTime(String judgeTime){
		this.judgeTime = judgeTime;
	}

	public String getJudgeTime(){
		return this.judgeTime;
	}

	public void setJudgeTimeStart(String judgeTimeStart){
		this.judgeTimeStart = judgeTimeStart;
	}

	public String getJudgeTimeStart(){
		return this.judgeTimeStart;
	}
	public void setJudgeTimeEnd(String judgeTimeEnd){
		this.judgeTimeEnd = judgeTimeEnd;
	}

	public String getJudgeTimeEnd(){
		return this.judgeTimeEnd;
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
