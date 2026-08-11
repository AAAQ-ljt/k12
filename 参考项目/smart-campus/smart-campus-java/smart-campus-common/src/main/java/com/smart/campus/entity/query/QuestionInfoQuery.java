package com.smart.campus.entity.query;

import java.util.Date;


/**
 * 习题题目表参数
 */
public class QuestionInfoQuery extends BaseParam {


	/**
	 * 主键ID
	 */
	private Integer questionId;

	/**
	 * 题目类型:1单选 2多选 3判断 4填空 5简答
	 */
	private Integer questionType;

	/**
	 * 题目标题
	 */
	private String questionTitle;

	private String questionTitleFuzzy;

	/**
	 * 题目配图，可为空，关联resource_info.resource_id多个用逗号隔开
	 */
	private String questionImage;

	private String questionImageFuzzy;

	/**
	 * 难度等级:1简单 2较易 3中等 4较难 5困难
	 */
	private Integer difficultyLevel;

	/**
	 * 标准答案，建议存JSON或统一文本，如果是选择题，存储选择题选项ID,exercise_question_option.option_id
	 */
	private String correctAnswer;

	private String correctAnswerFuzzy;

	/**
	 * 答案解析
	 */
	private String answerAnalysis;

	private String answerAnalysisFuzzy;

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


	public void setQuestionId(Integer questionId){
		this.questionId = questionId;
	}

	public Integer getQuestionId(){
		return this.questionId;
	}

	public void setQuestionType(Integer questionType){
		this.questionType = questionType;
	}

	public Integer getQuestionType(){
		return this.questionType;
	}

	public void setQuestionTitle(String questionTitle){
		this.questionTitle = questionTitle;
	}

	public String getQuestionTitle(){
		return this.questionTitle;
	}

	public void setQuestionTitleFuzzy(String questionTitleFuzzy){
		this.questionTitleFuzzy = questionTitleFuzzy;
	}

	public String getQuestionTitleFuzzy(){
		return this.questionTitleFuzzy;
	}

	public void setQuestionImage(String questionImage){
		this.questionImage = questionImage;
	}

	public String getQuestionImage(){
		return this.questionImage;
	}

	public void setQuestionImageFuzzy(String questionImageFuzzy){
		this.questionImageFuzzy = questionImageFuzzy;
	}

	public String getQuestionImageFuzzy(){
		return this.questionImageFuzzy;
	}

	public void setDifficultyLevel(Integer difficultyLevel){
		this.difficultyLevel = difficultyLevel;
	}

	public Integer getDifficultyLevel(){
		return this.difficultyLevel;
	}

	public void setCorrectAnswer(String correctAnswer){
		this.correctAnswer = correctAnswer;
	}

	public String getCorrectAnswer(){
		return this.correctAnswer;
	}

	public void setCorrectAnswerFuzzy(String correctAnswerFuzzy){
		this.correctAnswerFuzzy = correctAnswerFuzzy;
	}

	public String getCorrectAnswerFuzzy(){
		return this.correctAnswerFuzzy;
	}

	public void setAnswerAnalysis(String answerAnalysis){
		this.answerAnalysis = answerAnalysis;
	}

	public String getAnswerAnalysis(){
		return this.answerAnalysis;
	}

	public void setAnswerAnalysisFuzzy(String answerAnalysisFuzzy){
		this.answerAnalysisFuzzy = answerAnalysisFuzzy;
	}

	public String getAnswerAnalysisFuzzy(){
		return this.answerAnalysisFuzzy;
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
