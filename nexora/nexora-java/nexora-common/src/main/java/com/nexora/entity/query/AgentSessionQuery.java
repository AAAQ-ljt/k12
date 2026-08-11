package com.nexora.entity.query;

import java.util.Date;


/**
 * AI会话表参数
 */
public class AgentSessionQuery extends BaseParam {


	/**
	 * 会话ID
	 */
	private String sessionId;

	private String sessionIdFuzzy;

	/**
	 * 学生
	 */
	private Integer userId;

	/**
	 * 会话标题（首条消息摘要）
	 */
	private String title;

	private String titleFuzzy;

	/**
	 * 学段【冗余快照：会话创建时学段】
	 */
	private String stage;

	private String stageFuzzy;

	/**
	 * 当前学习知识点，可空
	 */
	private String knowledgePointId;

	private String knowledgePointIdFuzzy;

	/**
	 * 场景：0自由对话 1课程引导 2路径引导
	 */
	private Integer scene;

	/**
	 * 消息数【冗余：会话列表展示】
	 */
	private Integer messageCount;

	/**
	 * 最后消息时间【冗余：列表排序免max聚合】
	 */
	private String lastMessageTime;

	private String lastMessageTimeStart;

	private String lastMessageTimeEnd;

	/**
	 * 状态：0正常 1归档
	 */
	private Integer status;

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

	public void setUserId(Integer userId){
		this.userId = userId;
	}

	public Integer getUserId(){
		return this.userId;
	}

	public void setTitle(String title){
		this.title = title;
	}

	public String getTitle(){
		return this.title;
	}

	public void setTitleFuzzy(String titleFuzzy){
		this.titleFuzzy = titleFuzzy;
	}

	public String getTitleFuzzy(){
		return this.titleFuzzy;
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

	public void setScene(Integer scene){
		this.scene = scene;
	}

	public Integer getScene(){
		return this.scene;
	}

	public void setMessageCount(Integer messageCount){
		this.messageCount = messageCount;
	}

	public Integer getMessageCount(){
		return this.messageCount;
	}

	public void setLastMessageTime(String lastMessageTime){
		this.lastMessageTime = lastMessageTime;
	}

	public String getLastMessageTime(){
		return this.lastMessageTime;
	}

	public void setLastMessageTimeStart(String lastMessageTimeStart){
		this.lastMessageTimeStart = lastMessageTimeStart;
	}

	public String getLastMessageTimeStart(){
		return this.lastMessageTimeStart;
	}
	public void setLastMessageTimeEnd(String lastMessageTimeEnd){
		this.lastMessageTimeEnd = lastMessageTimeEnd;
	}

	public String getLastMessageTimeEnd(){
		return this.lastMessageTimeEnd;
	}

	public void setStatus(Integer status){
		this.status = status;
	}

	public Integer getStatus(){
		return this.status;
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
