package com.nexora.entity.query;

import java.util.Date;


/**
 * 消息主表参数
 */
public class MessageInfoQuery extends BaseParam {


	/**
	 * 消息ID
	 */
	private Integer messageId;

	/**
	 * 标题
	 */
	private String title;

	private String titleFuzzy;

	/**
	 * 内容
	 */
	private String content;

	private String contentFuzzy;

	/**
	 * 类型：0系统消息 1学习提醒
	 */
	private Integer messageType;

	/**
	 * 消息点击跳转路径，可空
	 */
	private String jumpPath;

	private String jumpPathFuzzy;

	/**
	 * 发送人（管理员）
	 */
	private Integer createBy;

	/**
	 * 创建时间
	 */
	private String createTime;

	private String createTimeStart;

	private String createTimeEnd;


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

	public void setTitleFuzzy(String titleFuzzy){
		this.titleFuzzy = titleFuzzy;
	}

	public String getTitleFuzzy(){
		return this.titleFuzzy;
	}

	public void setContent(String content){
		this.content = content;
	}

	public String getContent(){
		return this.content;
	}

	public void setContentFuzzy(String contentFuzzy){
		this.contentFuzzy = contentFuzzy;
	}

	public String getContentFuzzy(){
		return this.contentFuzzy;
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

	public void setJumpPathFuzzy(String jumpPathFuzzy){
		this.jumpPathFuzzy = jumpPathFuzzy;
	}

	public String getJumpPathFuzzy(){
		return this.jumpPathFuzzy;
	}

	public void setCreateBy(Integer createBy){
		this.createBy = createBy;
	}

	public Integer getCreateBy(){
		return this.createBy;
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
