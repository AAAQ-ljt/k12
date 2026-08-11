package com.smart.campus.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import com.smart.campus.entity.enums.DateTimePatternEnum;
import com.smart.campus.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 站内消息主表
 */
public class MessageInfo implements Serializable {


	/**
	 * 主键ID
	 */
	private Long messageId;

	/**
	 * 消息标题
	 */
	private String messageTitle;

	/**
	 * 消息内容
	 */
	private String messageContent;

	/**
	 * 消息类型: 1系统通知 2课程消息 3作业消息 4考试消息
	 */
	private Integer messageType;

	/**
	 * 业务类型: 0通用 1课程 2作业 3考试
	 */
	private Integer bizType;

	/**
	 * 业务ID，如 courseId/taskId/examId
	 */
	private String bizId;

	/**
	 * 发送人ID，系统消息可为空
	 */
	private Integer senderId;

	/**
	 * 发送人名称，冗余展示
	 */
	private String senderName;

	/**
	 * 发送范围: 1单人 2多人 3全体学生
	 */
	private Integer sendScope;

	/**
	 * 点击跳转路径
	 */
	private String jumpPath;

	/**
	 * 发送时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date sendTime;

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;


	public void setMessageId(Long messageId){
		this.messageId = messageId;
	}

	public Long getMessageId(){
		return this.messageId;
	}

	public void setMessageTitle(String messageTitle){
		this.messageTitle = messageTitle;
	}

	public String getMessageTitle(){
		return this.messageTitle;
	}

	public void setMessageContent(String messageContent){
		this.messageContent = messageContent;
	}

	public String getMessageContent(){
		return this.messageContent;
	}

	public void setMessageType(Integer messageType){
		this.messageType = messageType;
	}

	public Integer getMessageType(){
		return this.messageType;
	}

	public void setBizType(Integer bizType){
		this.bizType = bizType;
	}

	public Integer getBizType(){
		return this.bizType;
	}

	public void setBizId(String bizId){
		this.bizId = bizId;
	}

	public String getBizId(){
		return this.bizId;
	}

	public void setSenderId(Integer senderId){
		this.senderId = senderId;
	}

	public Integer getSenderId(){
		return this.senderId;
	}

	public void setSenderName(String senderName){
		this.senderName = senderName;
	}

	public String getSenderName(){
		return this.senderName;
	}

	public void setSendScope(Integer sendScope){
		this.sendScope = sendScope;
	}

	public Integer getSendScope(){
		return this.sendScope;
	}

	public void setJumpPath(String jumpPath){
		this.jumpPath = jumpPath;
	}

	public String getJumpPath(){
		return this.jumpPath;
	}

	public void setSendTime(Date sendTime){
		this.sendTime = sendTime;
	}

	public Date getSendTime(){
		return this.sendTime;
	}

	public void setCreateTime(Date createTime){
		this.createTime = createTime;
	}

	public Date getCreateTime(){
		return this.createTime;
	}

	@Override
	public String toString (){
		return "主键ID:"+(messageId == null ? "空" : messageId)+"，消息标题:"+(messageTitle == null ? "空" : messageTitle)+"，消息内容:"+(messageContent == null ? "空" : messageContent)+"，消息类型: 1系统通知 2课程消息 3作业消息 4考试消息:"+(messageType == null ? "空" : messageType)+"，业务类型: 0通用 1课程 2作业 3考试:"+(bizType == null ? "空" : bizType)+"，业务ID，如 courseId/taskId/examId:"+(bizId == null ? "空" : bizId)+"，发送人ID，系统消息可为空:"+(senderId == null ? "空" : senderId)+"，发送人名称，冗余展示:"+(senderName == null ? "空" : senderName)+"，发送范围: 1单人 2多人 3全体学生:"+(sendScope == null ? "空" : sendScope)+"，点击跳转路径:"+(jumpPath == null ? "空" : jumpPath)+"，发送时间:"+(sendTime == null ? "空" : DateUtil.format(sendTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
