package com.nexora.entity.query;

import java.util.Date;


/**
 * AI消息表参数
 */
public class AgentMessageQuery extends BaseParam {


	/**
	 * 消息ID（HTTP发送接口返回值）
	 */
	private String messageId;

	private String messageIdFuzzy;

	/**
	 * 会话ID
	 */
	private String sessionId;

	private String sessionIdFuzzy;

	/**
	 * 学生【冗余：学习分析免join会话表】
	 */
	private String userId;

	/**
	 * 学段【冗余快照】
	 */
	private String stage;

	private String stageFuzzy;

	/**
	 * 知识点，可空【冗余】
	 */
	private String knowledgePointId;

	private String knowledgePointIdFuzzy;

	/**
	 * 用户消息
	 */
	private String userMessage;

	private String userMessageFuzzy;

	/**
	 * AI回复（流式完成后落库）
	 */
	private String assistantMessage;

	private String assistantMessageFuzzy;

	/**
	 * 意图：EXPLAIN/RECOMMEND/QUIZ/PICTURE_BOOK/DRAW/ANIMATION/CODING/PLAN/PROGRESS/CHAT
	 */
	private String intent;

	private String intentFuzzy;

	/**
	 * 产物类型，可空：ANIMATION/PICTURE_BOOK/QUIZ/RESOURCE_LIST/CODE
	 */
	private String bizType;

	private String bizTypeFuzzy;

	/**
	 * 产物结构化JSON（卡片数据）
	 */
	private String bizData;

	private String bizDataFuzzy;

	/**
	 * 关联生成记录，可空
	 */
	private String generationId;

	private String generationIdFuzzy;

	/**
	 * 状态：0处理中 1完成 2取消 3错误
	 */
	private Integer status;

	/**
	 * 错误信息，可空
	 */
	private String errorInfo;

	private String errorInfoFuzzy;

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
	private String createTime;

	private String createTimeStart;

	private String createTimeEnd;

	/**
	 * 更新时间
	 */
	private String updateTime;

	private String updateTimeStart;

	private String updateTimeEnd;


	public void setMessageId(String messageId){
		this.messageId = messageId;
	}

	public String getMessageId(){
		return this.messageId;
	}

	public void setMessageIdFuzzy(String messageIdFuzzy){
		this.messageIdFuzzy = messageIdFuzzy;
	}

	public String getMessageIdFuzzy(){
		return this.messageIdFuzzy;
	}

	public void setSessionId(String sessionId){
		this.sessionId = sessionId;
	}

	public String getSessionId(){
		return this.sessionId;
	}

	public void setSessionIdFuzzy(String sessionIdFuzzy){
		this.sessionIdFuzzy = sessionIdFuzzy;
	}

	public String getSessionIdFuzzy(){
		return this.sessionIdFuzzy;
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

	public void setStageFuzzy(String stageFuzzy){
		this.stageFuzzy = stageFuzzy;
	}

	public String getStageFuzzy(){
		return this.stageFuzzy;
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

	public void setUserMessage(String userMessage){
		this.userMessage = userMessage;
	}

	public String getUserMessage(){
		return this.userMessage;
	}

	public void setUserMessageFuzzy(String userMessageFuzzy){
		this.userMessageFuzzy = userMessageFuzzy;
	}

	public String getUserMessageFuzzy(){
		return this.userMessageFuzzy;
	}

	public void setAssistantMessage(String assistantMessage){
		this.assistantMessage = assistantMessage;
	}

	public String getAssistantMessage(){
		return this.assistantMessage;
	}

	public void setAssistantMessageFuzzy(String assistantMessageFuzzy){
		this.assistantMessageFuzzy = assistantMessageFuzzy;
	}

	public String getAssistantMessageFuzzy(){
		return this.assistantMessageFuzzy;
	}

	public void setIntent(String intent){
		this.intent = intent;
	}

	public String getIntent(){
		return this.intent;
	}

	public void setIntentFuzzy(String intentFuzzy){
		this.intentFuzzy = intentFuzzy;
	}

	public String getIntentFuzzy(){
		return this.intentFuzzy;
	}

	public void setBizType(String bizType){
		this.bizType = bizType;
	}

	public String getBizType(){
		return this.bizType;
	}

	public void setBizTypeFuzzy(String bizTypeFuzzy){
		this.bizTypeFuzzy = bizTypeFuzzy;
	}

	public String getBizTypeFuzzy(){
		return this.bizTypeFuzzy;
	}

	public void setBizData(String bizData){
		this.bizData = bizData;
	}

	public String getBizData(){
		return this.bizData;
	}

	public void setBizDataFuzzy(String bizDataFuzzy){
		this.bizDataFuzzy = bizDataFuzzy;
	}

	public String getBizDataFuzzy(){
		return this.bizDataFuzzy;
	}

	public void setGenerationId(String generationId){
		this.generationId = generationId;
	}

	public String getGenerationId(){
		return this.generationId;
	}

	public void setGenerationIdFuzzy(String generationIdFuzzy){
		this.generationIdFuzzy = generationIdFuzzy;
	}

	public String getGenerationIdFuzzy(){
		return this.generationIdFuzzy;
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

	public void setErrorInfoFuzzy(String errorInfoFuzzy){
		this.errorInfoFuzzy = errorInfoFuzzy;
	}

	public String getErrorInfoFuzzy(){
		return this.errorInfoFuzzy;
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
