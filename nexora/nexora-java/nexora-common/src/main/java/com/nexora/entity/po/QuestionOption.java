package com.nexora.entity.po;

import java.util.Date;
import com.nexora.entity.enums.DateTimePatternEnum;
import com.nexora.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 题目选项表
 */
public class QuestionOption implements Serializable {


	/**
	 * 选项ID
	 */
	private Integer optionId;

	/**
	 * 题目ID
	 */
	private String questionId;

	/**
	 * 选项标号：A/B/C/D
	 */
	private String optionLabel;

	/**
	 * 选项内容
	 */
	private String optionContent;

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
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;


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

	public void setOptionLabel(String optionLabel){
		this.optionLabel = optionLabel;
	}

	public String getOptionLabel(){
		return this.optionLabel;
	}

	public void setOptionContent(String optionContent){
		this.optionContent = optionContent;
	}

	public String getOptionContent(){
		return this.optionContent;
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

	public void setCreateTime(Date createTime){
		this.createTime = createTime;
	}

	public Date getCreateTime(){
		return this.createTime;
	}

	@Override
	public String toString (){
		return "选项ID:"+(optionId == null ? "空" : optionId)+"，题目ID:"+(questionId == null ? "空" : questionId)+"，选项标号：A/B/C/D:"+(optionLabel == null ? "空" : optionLabel)+"，选项内容:"+(optionContent == null ? "空" : optionContent)+"，0否 1是:"+(isAnswer == null ? "空" : isAnswer)+"，排序:"+(sort == null ? "空" : sort)+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
