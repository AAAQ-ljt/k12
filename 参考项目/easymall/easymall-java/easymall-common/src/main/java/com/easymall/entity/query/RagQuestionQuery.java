package com.easymall.entity.query;

/**
 * rag问题参数
 */
public class RagQuestionQuery extends BaseParam {


	/**
	 * 自增ID
	 */
	private Integer questionId;

	/**
	 * 问题
	 */
	private String question;

	private String questionFuzzy;

	/**
	 * 相似问题
	 */
	private String similarQuestion;

	private String similarQuestionFuzzy;

	/**
	 * 答案
	 */
	private String answer;

	private String answerFuzzy;

	/**
	 * 创建时间
	 */
	private String createTime;

	private String createTimeStart;

	private String createTimeEnd;


	public void setQuestionId(Integer questionId){
		this.questionId = questionId;
	}

	public Integer getQuestionId(){
		return this.questionId;
	}

	public void setQuestion(String question){
		this.question = question;
	}

	public String getQuestion(){
		return this.question;
	}

	public void setQuestionFuzzy(String questionFuzzy){
		this.questionFuzzy = questionFuzzy;
	}

	public String getQuestionFuzzy(){
		return this.questionFuzzy;
	}

	public void setSimilarQuestion(String similarQuestion){
		this.similarQuestion = similarQuestion;
	}

	public String getSimilarQuestion(){
		return this.similarQuestion;
	}

	public void setSimilarQuestionFuzzy(String similarQuestionFuzzy){
		this.similarQuestionFuzzy = similarQuestionFuzzy;
	}

	public String getSimilarQuestionFuzzy(){
		return this.similarQuestionFuzzy;
	}

	public void setAnswer(String answer){
		this.answer = answer;
	}

	public String getAnswer(){
		return this.answer;
	}

	public void setAnswerFuzzy(String answerFuzzy){
		this.answerFuzzy = answerFuzzy;
	}

	public String getAnswerFuzzy(){
		return this.answerFuzzy;
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
