package com.nexora.entity.po;

import java.util.Date;
import com.nexora.entity.enums.DateTimePatternEnum;
import com.nexora.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 用户消息关联表
 */
public class MessageUser implements Serializable {


	/**
	 * 主键
	 */
	private Integer id;

	/**
	 * 消息ID
	 */
	private Integer messageId;

	/**
	 * 接收人
	 */
	private Integer userId;

	/**
	 * 0未读 1已读
	 */
	private Integer readStatus;

	/**
	 * 阅读时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date readTime;

	/**
	 * 0正常 1已删除（学生隐藏消息）
	 */
	private Integer deleteFlag;

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;


	public void setId(Integer id){
		this.id = id;
	}

	public Integer getId(){
		return this.id;
	}

	public void setMessageId(Integer messageId){
		this.messageId = messageId;
	}

	public Integer getMessageId(){
		return this.messageId;
	}

	public void setUserId(Integer userId){
		this.userId = userId;
	}

	public Integer getUserId(){
		return this.userId;
	}

	public void setReadStatus(Integer readStatus){
		this.readStatus = readStatus;
	}

	public Integer getReadStatus(){
		return this.readStatus;
	}

	public void setReadTime(Date readTime){
		this.readTime = readTime;
	}

	public Date getReadTime(){
		return this.readTime;
	}

	public void setDeleteFlag(Integer deleteFlag){
		this.deleteFlag = deleteFlag;
	}

	public Integer getDeleteFlag(){
		return this.deleteFlag;
	}

	public void setCreateTime(Date createTime){
		this.createTime = createTime;
	}

	public Date getCreateTime(){
		return this.createTime;
	}

	@Override
	public String toString (){
		return "主键:"+(id == null ? "空" : id)+"，消息ID:"+(messageId == null ? "空" : messageId)+"，接收人:"+(userId == null ? "空" : userId)+"，0未读 1已读:"+(readStatus == null ? "空" : readStatus)+"，阅读时间:"+(readTime == null ? "空" : DateUtil.format(readTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，0正常 1已删除（学生隐藏消息）:"+(deleteFlag == null ? "空" : deleteFlag)+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
