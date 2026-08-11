package com.smart.campus.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import com.smart.campus.entity.enums.DateTimePatternEnum;
import com.smart.campus.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 用户消息收件表
 */
public class MessageUser implements Serializable {


	/**
	 * 主键ID
	 */
	private Long id;

	/**
	 * 消息ID
	 */
	private Long messageId;

	/**
	 * 接收用户ID
	 */
	private Integer userId;

	/**
	 * 是否已读: 0否 1是
	 */
	private Integer readFlag;

	/**
	 * 阅读时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date readTime;

	/**
	 * 是否删除: 0否 1是
	 */
	private Integer deleteFlag;

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;


	public void setId(Long id){
		this.id = id;
	}

	public Long getId(){
		return this.id;
	}

	public void setMessageId(Long messageId){
		this.messageId = messageId;
	}

	public Long getMessageId(){
		return this.messageId;
	}

	public void setUserId(Integer userId){
		this.userId = userId;
	}

	public Integer getUserId(){
		return this.userId;
	}

	public void setReadFlag(Integer readFlag){
		this.readFlag = readFlag;
	}

	public Integer getReadFlag(){
		return this.readFlag;
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
		return "主键ID:"+(id == null ? "空" : id)+"，消息ID:"+(messageId == null ? "空" : messageId)+"，接收用户ID:"+(userId == null ? "空" : userId)+"，是否已读: 0否 1是:"+(readFlag == null ? "空" : readFlag)+"，阅读时间:"+(readTime == null ? "空" : DateUtil.format(readTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，是否删除: 0否 1是:"+(deleteFlag == null ? "空" : deleteFlag)+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
