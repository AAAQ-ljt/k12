package com.nexora.entity.po;

import java.util.Date;
import com.nexora.entity.enums.DateTimePatternEnum;
import com.nexora.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 消息主表
 */
public class MessageInfo implements Serializable {


	/**
	 * 消息ID
	 */
	private Integer messageId;

	/**
	 * 标题
	 */
	private String title;

	/**
	 * 内容
	 */
	private String content;

	/**
	 * 类型：0系统消息 1学习提醒
	 */
	private Integer messageType;

	/**
	 * 消息点击跳转路径，可空
	 */
	private String jumpPath;

	/**
	 * 发送人（管理员）
	 */
	private Integer createBy;

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;


	public void setMessageId(Integer messageId){
		this.messageId = messageId;
	}

	public Integer getMessageId(){
		return this.messageId;
	}

	public void setTitle(String title){
		this.title = title;
	}

	public String getTitle(){
		return this.title;
	}

	public void setContent(String content){
		this.content = content;
	}

	public String getContent(){
		return this.content;
	}

	public void setMessageType(Integer messageType){
		this.messageType = messageType;
	}

	public Integer getMessageType(){
		return this.messageType;
	}

	public void setJumpPath(String jumpPath){
		this.jumpPath = jumpPath;
	}

	public String getJumpPath(){
		return this.jumpPath;
	}

	public void setCreateBy(Integer createBy){
		this.createBy = createBy;
	}

	public Integer getCreateBy(){
		return this.createBy;
	}

	public void setCreateTime(Date createTime){
		this.createTime = createTime;
	}

	public Date getCreateTime(){
		return this.createTime;
	}

	@Override
	public String toString (){
		return "消息ID:"+(messageId == null ? "空" : messageId)+"，标题:"+(title == null ? "空" : title)+"，内容:"+(content == null ? "空" : content)+"，类型：0系统消息 1学习提醒:"+(messageType == null ? "空" : messageType)+"，消息点击跳转路径，可空:"+(jumpPath == null ? "空" : jumpPath)+"，发送人（管理员）:"+(createBy == null ? "空" : createBy)+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
