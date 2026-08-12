package com.nexora.entity.po;

import java.util.Date;
import com.nexora.entity.enums.DateTimePatternEnum;
import com.nexora.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * AI消息表
 */
public class AgentMessage implements Serializable {


	/**
	 * 消息ID（HTTP发送接口返回值）
	 */
	private String messageId;

	/**
	 * 会话ID
	 */
	private String sessionId;

	/**
	 * 学生【冗余：学习分析免join会话表】
	 */
	private String userId;

	/**
	 * 学段【冗余快照】
	 */
	private String stage;

	/**
	 * 知识点，可空【冗余】
	 */
	private String knowledgePointId;

	/**
	 * 用户消息
	 */
	private String userMessage;

	/**
	 * AI回复（流式完成后落库）
	 */
	private String assistantMessage;

	/**
	 * 意图：EXPLAIN/RECOMMEND/QUIZ/PICTURE_BOOK/DRAW/ANIMATION/CODING/PLAN/PROGRESS/CHAT
	 */
	private String intent;

	/**
	 * 产物类型，可空：ANIMATION/PICTURE_BOOK/QUIZ/RESOURCE_LIST/CODE
	 */
	private String bizType;

	/**
	 * 产物结构化JSON（卡片数据）
	 */
	private String bizData;

	/**
	 * 关联生成记录，可空
	 */
	private String generationId;

	/**
	 * 状态：0处理中 1完成 2取消 3错误
	 */
	private Integer status;

	/**
	 * 错误信息，可空
	 */
	private String errorInfo;

	/**
	 * 输入token用量，默认0
	 */
	private Integer promptTokens;

	/**
	 * 输出token用量，默认0
	 */
	private Integer completionTokens;

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	/**
	 * 更新时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date updateTime;


	public void setMessageId(String messageId){
		this.messageId = messageId;
	}

	public String getMessageId(){
		return this.messageId;
	}

	public void setSessionId(String sessionId){
		this.sessionId = sessionId;
	}

	public String getSessionId(){
		return this.sessionId;
	}

	public void setUserId(String userId){
		this.userId = userId;
	}

	public String getUserId(){
		return this.userId;
	}

	public void setStage(String stage){
		this.stage = stage;
	}

	public String getStage(){
		return this.stage;
	}

	public void setKnowledgePointId(String knowledgePointId){
		this.knowledgePointId = knowledgePointId;
	}

	public String getKnowledgePointId(){
		return this.knowledgePointId;
	}

	public void setUserMessage(String userMessage){
		this.userMessage = userMessage;
	}

	public String getUserMessage(){
		return this.userMessage;
	}

	public void setAssistantMessage(String assistantMessage){
		this.assistantMessage = assistantMessage;
	}

	public String getAssistantMessage(){
		return this.assistantMessage;
	}

	public void setIntent(String intent){
		this.intent = intent;
	}

	public String getIntent(){
		return this.intent;
	}

	public void setBizType(String bizType){
		this.bizType = bizType;
	}

	public String getBizType(){
		return this.bizType;
	}

	public void setBizData(String bizData){
		this.bizData = bizData;
	}

	public String getBizData(){
		return this.bizData;
	}

	public void setGenerationId(String generationId){
		this.generationId = generationId;
	}

	public String getGenerationId(){
		return this.generationId;
	}

	public void setStatus(Integer status){
		this.status = status;
	}

	public Integer getStatus(){
		return this.status;
	}

	public void setErrorInfo(String errorInfo){
		this.errorInfo = errorInfo;
	}

	public String getErrorInfo(){
		return this.errorInfo;
	}

	public void setPromptTokens(Integer promptTokens){
		this.promptTokens = promptTokens;
	}

	public Integer getPromptTokens(){
		return this.promptTokens;
	}

	public void setCompletionTokens(Integer completionTokens){
		this.completionTokens = completionTokens;
	}

	public Integer getCompletionTokens(){
		return this.completionTokens;
	}

	public void setCreateTime(Date createTime){
		this.createTime = createTime;
	}

	public Date getCreateTime(){
		return this.createTime;
	}

	public void setUpdateTime(Date updateTime){
		this.updateTime = updateTime;
	}

	public Date getUpdateTime(){
		return this.updateTime;
	}

	@Override
	public String toString (){
		return "消息ID（HTTP发送接口返回值）:"+(messageId == null ? "空" : messageId)+"，会话ID:"+(sessionId == null ? "空" : sessionId)+"，学生【冗余：学习分析免join会话表】:"+(userId == null ? "空" : userId)+"，学段【冗余快照】:"+(stage == null ? "空" : stage)+"，知识点，可空【冗余】:"+(knowledgePointId == null ? "空" : knowledgePointId)+"，用户消息:"+(userMessage == null ? "空" : userMessage)+"，AI回复（流式完成后落库）:"+(assistantMessage == null ? "空" : assistantMessage)+"，意图：EXPLAIN/RECOMMEND/QUIZ/PICTURE_BOOK/DRAW/ANIMATION/CODING/PLAN/PROGRESS/CHAT:"+(intent == null ? "空" : intent)+"，产物类型，可空：ANIMATION/PICTURE_BOOK/QUIZ/RESOURCE_LIST/CODE:"+(bizType == null ? "空" : bizType)+"，产物结构化JSON（卡片数据）:"+(bizData == null ? "空" : bizData)+"，关联生成记录，可空:"+(generationId == null ? "空" : generationId)+"，状态：0处理中 1完成 2取消 3错误:"+(status == null ? "空" : status)+"，错误信息，可空:"+(errorInfo == null ? "空" : errorInfo)+"，输入token用量，默认0:"+(promptTokens == null ? "空" : promptTokens)+"，输出token用量，默认0:"+(completionTokens == null ? "空" : completionTokens)+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，更新时间:"+(updateTime == null ? "空" : DateUtil.format(updateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
