package com.smart.campus.entity.query;

import java.math.BigDecimal;


/**
 * 课程作业/考试学生答题明细表参数
 */
public class CourseAssessmentSubmitQuestionQuery extends BaseParam {


	/**
	 * 主键ID
	 */
	private Long id;

	/**
	 * 提交ID，对应 course_assessment_submit.submit_id
	 */
	private Long submitId;

	/**
	 * 任务ID，对应 course_assessment_task.task_id
	 */
	private Long taskId;

	/**
	 * 试卷题目编排ID，对应 paper_info.paper_id
	 */
	private Integer paperId;

	/**
	 * 题目ID，对应 question_info.question_id
	 */
	private Integer questionId;

	/**
	 * 学生答案，建议存JSON
	 */
	private String answerContent;

	private String answerContentFuzzy;

	/**
	 * 最终得分
	 */
	private BigDecimal finalScore;

	/**
	 * 判题状态: 0未判 1自动判分完成 2待人工批改 3人工批改完成
	 */
	private Integer judgeStatus;


	public void setId(Long id){
		this.id = id;
	}

	public Long getId(){
		return this.id;
	}

	public void setSubmitId(Long submitId){
		this.submitId = submitId;
	}

	public Long getSubmitId(){
		return this.submitId;
	}

	public void setTaskId(Long taskId){
		this.taskId = taskId;
	}

	public Long getTaskId(){
		return this.taskId;
	}

	public void setPaperId(Integer paperId){
		this.paperId = paperId;
	}

	public Integer getPaperId(){
		return this.paperId;
	}

	public void setQuestionId(Integer questionId){
		this.questionId = questionId;
	}

	public Integer getQuestionId(){
		return this.questionId;
	}

	public void setAnswerContent(String answerContent){
		this.answerContent = answerContent;
	}

	public String getAnswerContent(){
		return this.answerContent;
	}

	public void setAnswerContentFuzzy(String answerContentFuzzy){
		this.answerContentFuzzy = answerContentFuzzy;
	}

	public String getAnswerContentFuzzy(){
		return this.answerContentFuzzy;
	}

	public void setFinalScore(BigDecimal finalScore){
		this.finalScore = finalScore;
	}

	public BigDecimal getFinalScore(){
		return this.finalScore;
	}

	public void setJudgeStatus(Integer judgeStatus){
		this.judgeStatus = judgeStatus;
	}

	public Integer getJudgeStatus(){
		return this.judgeStatus;
	}

}
