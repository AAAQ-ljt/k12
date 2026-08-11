package com.nexora.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import com.nexora.entity.enums.DateTimePatternEnum;
import com.nexora.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 游戏化练习记录表
 */
public class PracticeRecord implements Serializable {


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

	/**
	 * 学段【冗余快照：按学段分析免join】
	 */
	private String stage;

	/**
	 * 题目ID
	 */
	private String questionId;

	/**
	 * 题型【冗余快照】
	 */
	private Integer questionType;

	/**
	 * 学生作答
	 */
	private String userAnswer;

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
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;


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

	public void setStage(String stage){
		this.stage = stage;
	}

	public String getStage(){
		return this.stage;
	}

	public void setQuestionId(String questionId){
		this.questionId = questionId;
	}

	public String getQuestionId(){
		return this.questionId;
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

	public void setCreateTime(Date createTime){
		this.createTime = createTime;
	}

	public Date getCreateTime(){
		return this.createTime;
	}

	@Override
	public String toString (){
		return "记录ID:"+(recordId == null ? "空" : recordId)+"，学生:"+(userId == null ? "空" : userId)+"，知识点【冗余快照：提交时从题目复制】:"+(knowledgePointId == null ? "空" : knowledgePointId)+"，学段【冗余快照：按学段分析免join】:"+(stage == null ? "空" : stage)+"，题目ID:"+(questionId == null ? "空" : questionId)+"，题型【冗余快照】:"+(questionType == null ? "空" : questionType)+"，学生作答:"+(userAnswer == null ? "空" : userAnswer)+"，0错 1对:"+(isCorrect == null ? "空" : isCorrect)+"，得分:"+(score == null ? "空" : score)+"，用时（秒）:"+(duration == null ? "空" : duration)+"，来源：0对话练习 1路径快测 2遗忘复习:"+(source == null ? "空" : source)+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
