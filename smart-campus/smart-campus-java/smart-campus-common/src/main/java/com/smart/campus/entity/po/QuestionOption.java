package com.smart.campus.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;


/**
 * 习题选项表
 */
public class QuestionOption implements Serializable {


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

	public void setSortOrder(Integer sortOrder){
		this.sortOrder = sortOrder;
	}

	public Integer getSortOrder(){
		return this.sortOrder;
	}

	@Override
	public String toString (){
		return "主键ID:"+(optionId == null ? "空" : optionId)+"，题目ID，对应exercise_question.question_id:"+(questionId == null ? "空" : questionId)+"，选项内容:"+(optionContent == null ? "空" : optionContent)+"，排序值:"+(sortOrder == null ? "空" : sortOrder);
	}
}
