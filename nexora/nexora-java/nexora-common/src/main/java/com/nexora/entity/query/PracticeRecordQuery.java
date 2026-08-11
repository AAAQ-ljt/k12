package com.nexora.entity.query;

import java.util.Date;


/**
 * 游戏化练习记录表参数
 */
public class PracticeRecordQuery extends BaseParam {


	/**
	 * 记录ID
	 */
	private Long recordId;

	/**
	 * 学生
	 */
	private Integer userId;

	/**
	 * 知识点【冗余快照：提交时从题目复制】
	 */
	private String knowledgePointId;

	private String knowledgePointIdFuzzy;

	/**
	 * 学段【冗余快照：按学段分析免join】
	 */
	private String stage;

	private String stageFuzzy;

	/**
	 * 题目ID
	 */
	private String questionId;

	private String questionIdFuzzy;

	/**
	 * 题型【冗余快照】
	 */
	private Integer questionType;

	/**
	 * 学生作答
	 */
	private String userAnswer;

	private String userAnswerFuzzy;

	/**
	 * 0错 1对
	 */
	private Integer isCorrect;

	/**
	 * 得分
	 */
	private Integer score;

	/**
	 * 用时（秒）
	 */
	private Integer duration;

	/**
	 * 来源：0对话练习 1路径快测 2遗忘复习
	 */
	private Integer source;

	/**
	 * 创建时间
	 */
	private String createTime;

	private String createTimeStart;

	private String createTimeEnd;


	public void setRecordId(Long recordId){
		this.recordId = recordId;
	}

	public Long getRecordId(){
		return this.recordId;
	}

	public void setUserId(Integer userId){
		this.userId = userId;
	}

	public Integer getUserId(){
		return this.userId;
	}

	public void setKnowledgePointId(String knowledgePointId){
		this.knowledgePointId = knowledgePointId;
	}

	public String getKnowledgePointId(){
		return this.knowledgePointId;
	}

	public void setKnowledgePointIdFuzzy(String knowledgePointIdFuzzy){
		this.knowledgePointIdFuzzy = knowledgePointIdFuzzy;
	}

	public String getKnowledgePointIdFuzzy(){
		return this.knowledgePointIdFuzzy;
	}

	public void setStage(String stage){
		this.stage = stage;
	}

	public String getStage(){
		return this.stage;
	}

	public void setStageFuzzy(String stageFuzzy){
		this.stageFuzzy = stageFuzzy;
	}

	public String getStageFuzzy(){
		return this.stageFuzzy;
	}

	public void setQuestionId(String questionId){
		this.questionId = questionId;
	}

	public String getQuestionId(){
		return this.questionId;
	}

	public void setQuestionIdFuzzy(String questionIdFuzzy){
		this.questionIdFuzzy = questionIdFuzzy;
	}

	public String getQuestionIdFuzzy(){
		return this.questionIdFuzzy;
	}

	public void setQuestionType(Integer questionType){
		this.questionType = questionType;
	}

	public Integer getQuestionType(){
		return this.questionType;
	}

	public void setUserAnswer(String userAnswer){
		this.userAnswer = userAnswer;
	}

	public String getUserAnswer(){
		return this.userAnswer;
	}

	public void setUserAnswerFuzzy(String userAnswerFuzzy){
		this.userAnswerFuzzy = userAnswerFuzzy;
	}

	public String getUserAnswerFuzzy(){
		return this.userAnswerFuzzy;
	}

	public void setIsCorrect(Integer isCorrect){
		this.isCorrect = isCorrect;
	}

	public Integer getIsCorrect(){
		return this.isCorrect;
	}

	public void setScore(Integer score){
		this.score = score;
	}

	public Integer getScore(){
		return this.score;
	}

	public void setDuration(Integer duration){
		this.duration = duration;
	}

	public Integer getDuration(){
		return this.duration;
	}

	public void setSource(Integer source){
		this.source = source;
	}

	public Integer getSource(){
		return this.source;
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
