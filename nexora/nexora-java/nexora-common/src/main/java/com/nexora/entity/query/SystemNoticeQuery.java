package com.nexora.entity.query;

import java.util.Date;


/**
 * 系统通知表参数
 */
public class SystemNoticeQuery extends BaseParam {


	/**
	 * 通知ID
	 */
	private Integer noticeId;

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
	 * 状态：0草稿 1已发布
	 */
	private Integer status;

	/**
	 * 发布时间
	 */
	private String publishTime;

	private String publishTimeStart;

	private String publishTimeEnd;

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


	public void setNoticeId(Integer noticeId){
		this.noticeId = noticeId;
	}

	public Integer getNoticeId(){
		return this.noticeId;
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

	public void setStatus(Integer status){
		this.status = status;
	}

	public Integer getStatus(){
		return this.status;
	}

	public void setPublishTime(String publishTime){
		this.publishTime = publishTime;
	}

	public String getPublishTime(){
		return this.publishTime;
	}

	public void setPublishTimeStart(String publishTimeStart){
		this.publishTimeStart = publishTimeStart;
	}

	public String getPublishTimeStart(){
		return this.publishTimeStart;
	}
	public void setPublishTimeEnd(String publishTimeEnd){
		this.publishTimeEnd = publishTimeEnd;
	}

	public String getPublishTimeEnd(){
		return this.publishTimeEnd;
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
