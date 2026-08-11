package com.nexora.entity.query;

import java.util.Date;


/**
 * 用户消息关联表参数
 */
public class MessageUserQuery extends BaseParam {


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
	private String readTime;

	private String readTimeStart;

	private String readTimeEnd;

	/**
	 * 0正常 1已删除（学生隐藏消息）
	 */
	private Integer deleteFlag;

	/**
	 * 创建时间
	 */
	private String createTime;

	private String createTimeStart;

	private String createTimeEnd;


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

	public void setReadTime(String readTime){
		this.readTime = readTime;
	}

	public String getReadTime(){
		return this.readTime;
	}

	public void setReadTimeStart(String readTimeStart){
		this.readTimeStart = readTimeStart;
	}

	public String getReadTimeStart(){
		return this.readTimeStart;
	}
	public void setReadTimeEnd(String readTimeEnd){
		this.readTimeEnd = readTimeEnd;
	}

	public String getReadTimeEnd(){
		return this.readTimeEnd;
	}

	public void setDeleteFlag(Integer deleteFlag){
		this.deleteFlag = deleteFlag;
	}

	public Integer getDeleteFlag(){
		return this.deleteFlag;
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

}
