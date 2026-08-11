package com.smart.campus.entity.query;



/**
 * 习题选项表参数
 */
public class QuestionOptionQuery extends BaseParam {


	/**
	 * 主键ID
	 */
	private Integer optionId;

	/**
	 * 题目ID，对应exercise_question.question_id
	 */
	private Integer questionId;

	/**
	 * 选项内容
	 */
	private String optionContent;

	private String optionContentFuzzy;

	/**
	 * 排序值
	 */
	private Integer sortOrder;


	public void setOptionId(Integer optionId){
		this.optionId = optionId;
	}

	public Integer getOptionId(){
		return this.optionId;
	}

	public void setQuestionId(Integer questionId){
		this.questionId = questionId;
	}

	public Integer getQuestionId(){
		return this.questionId;
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

	public void setSortOrder(Integer sortOrder){
		this.sortOrder = sortOrder;
	}

	public Integer getSortOrder(){
		return this.sortOrder;
	}

}
