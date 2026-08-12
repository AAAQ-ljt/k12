package com.nexora.entity.po;

import java.util.Date;
import com.nexora.entity.enums.DateTimePatternEnum;
import com.nexora.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * AI会话表
 */
public class AgentSession implements Serializable {


	/**
	 * 会话ID
	 */
	private String sessionId;

	/**
	 * 学生
	 */
	private Integer userId;

	/**
	 * 会话标题（首条消息摘要）
	 */
	private String title;

	/**
	 * 学段【冗余快照：会话创建时学段】
	 */
	private String stage;

	/**
	 * 当前学习知识点，可空
	 */
	private String knowledgePointId;

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
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lastMessageTime;

	/**
	 * 状态：0正常 1归档
	 */
	private Integer status;

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


	public void setSessionId(String sessionId){
		this.sessionId = sessionId;
	}

	public String getSessionId(){
		return this.sessionId;
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

	public void setLastMessageTime(Date lastMessageTime){
		this.lastMessageTime = lastMessageTime;
	}

	public Date getLastMessageTime(){
		return this.lastMessageTime;
	}

	public void setStatus(Integer status){
		this.status = status;
	}

	public Integer getStatus(){
		return this.status;
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
		return "会话ID:"+(sessionId == null ? "空" : sessionId)+"，学生:"+(userId == null ? "空" : userId)+"，会话标题（首条消息摘要）:"+(title == null ? "空" : title)+"，学段【冗余快照：会话创建时学段】:"+(stage == null ? "空" : stage)+"，当前学习知识点，可空:"+(knowledgePointId == null ? "空" : knowledgePointId)+"，场景：0自由对话 1课程引导 2路径引导:"+(scene == null ? "空" : scene)+"，消息数【冗余：会话列表展示】:"+(messageCount == null ? "空" : messageCount)+"，最后消息时间【冗余：列表排序免max聚合】:"+(lastMessageTime == null ? "空" : DateUtil.format(lastMessageTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，状态：0正常 1归档:"+(status == null ? "空" : status)+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，更新时间:"+(updateTime == null ? "空" : DateUtil.format(updateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
