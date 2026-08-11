package com.nexora.entity.query;

import java.util.Date;


/**
 * 题目选项表参数
 */
public class QuestionOptionQuery extends BaseParam {


	/**
	 * 选项ID
	 */
	private Integer optionId;

	/**
	 * 题目ID
	 */
	private String questionId;

	private String questionIdFuzzy;

	/**
	 * 选项标号：A/B/C/D
	 */
	private String optionLabel;

	private String optionLabelFuzzy;

	/**
	 * 选项内容
	 */
	private String optionContent;

	private String optionContentFuzzy;

	/**
	 * 0否 1是
	 */
	private Integer isAnswer;

	/**
	 * 排序
	 */
	private Integer sort;

	/**
	 * 创建时间
	 */
	private String createTime;

	private String createTimeStart;

	private String createTimeEnd;


	public void setOptionId(Integer optionId){
		this.optionId = optionId;
	}

	public Integer getOptionId(){
		return this.optionId;
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

	public void setOptionLabel(String optionLabel){
		this.optionLabel = optionLabel;
	}

	public String getOptionLabel(){
		return this.optionLabel;
	}

	public void setOptionLabelFuzzy(String optionLabelFuzzy){
		this.optionLabelFuzzy = optionLabelFuzzy;
	}

	public String getOptionLabelFuzzy(){
		return this.optionLabelFuzzy;
	}

	public void setOptionContent(String optionContent){
		this.optionContent = optionContent;
	}

	public String getOptionContent(){
		return this.optionContent;
	}

	public void setOptionContentFuzzy(String optionContentFuzzy){
		this.optionContentFuzzy = optionContentFuzzy;
	}

	public String getOptionContentFuzzy(){
		return this.optionContentFuzzy;
	}

	public void setIsAnswer(Integer isAnswer){
		this.isAnswer = isAnswer;
	}

	public Integer getIsAnswer(){
		return this.isAnswer;
	}

	public void setSort(Integer sort){
		this.sort = sort;
	}

	public Integer getSort(){
		return this.sort;
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
