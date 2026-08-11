package com.smart.campus.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import com.smart.campus.entity.enums.DateTimePatternEnum;
import com.smart.campus.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 系统通知公告表
 */
public class SystemNotice implements Serializable {


	/**
	 * 通知ID
	 */
	private String noticeId;

	/**
	 * 通知标题
	 */
	private String noticeTitle;

	/**
	 * 通知内容
	 */
	private String noticeContent;

	/**
	 * 发布范围: 1全部学生 2指定班级 3指定专业
	 */
	private Integer targetType;

	/**
	 * 发布目标ID，多个用英文逗号分隔
	 */
	private String targetIds;

	/**
	 * 状态: 0草稿 1已发布 2已下线
	 */
	private Integer status;

	/**
	 * 是否置顶: 0否 1是
	 */
	private Integer isTop;

	/**
	 * 发布时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date publishTime;

	/**
	 * 浏览次数
	 */
	private Integer viewCount;

	/**
	 * 创建人ID
	 */
	private Integer createUserId;

	/**
	 * 创建人姓名
	 */
	private String createUserName;

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	/**
	 * 最后更新时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lastUpdateTime;


	public void setNoticeId(String noticeId){
		this.noticeId = noticeId;
	}

	public String getNoticeId(){
		return this.noticeId;
	}

	public void setNoticeTitle(String noticeTitle){
		this.noticeTitle = noticeTitle;
	}

	public String getNoticeTitle(){
		return this.noticeTitle;
	}

	public void setNoticeContent(String noticeContent){
		this.noticeContent = noticeContent;
	}

	public String getNoticeContent(){
		return this.noticeContent;
	}

	public void setTargetType(Integer targetType){
		this.targetType = targetType;
	}

	public Integer getTargetType(){
		return this.targetType;
	}

	public void setTargetIds(String targetIds){
		this.targetIds = targetIds;
	}

	public String getTargetIds(){
		return this.targetIds;
	}

	public void setStatus(Integer status){
		this.status = status;
	}

	public Integer getStatus(){
		return this.status;
	}

	public void setIsTop(Integer isTop){
		this.isTop = isTop;
	}

	public Integer getIsTop(){
		return this.isTop;
	}

	public void setPublishTime(Date publishTime){
		this.publishTime = publishTime;
	}

	public Date getPublishTime(){
		return this.publishTime;
	}

	public void setViewCount(Integer viewCount){
		this.viewCount = viewCount;
	}

	public Integer getViewCount(){
		return this.viewCount;
	}

	public void setCreateUserId(Integer createUserId){
		this.createUserId = createUserId;
	}

	public Integer getCreateUserId(){
		return this.createUserId;
	}

	public void setCreateUserName(String createUserName){
		this.createUserName = createUserName;
	}

	public String getCreateUserName(){
		return this.createUserName;
	}

	public void setCreateTime(Date createTime){
		this.createTime = createTime;
	}

	public Date getCreateTime(){
		return this.createTime;
	}

	public void setLastUpdateTime(Date lastUpdateTime){
		this.lastUpdateTime = lastUpdateTime;
	}

	public Date getLastUpdateTime(){
		return this.lastUpdateTime;
	}

	@Override
	public String toString (){
		return "通知ID:"+(noticeId == null ? "空" : noticeId)+"，通知标题:"+(noticeTitle == null ? "空" : noticeTitle)+"，通知内容:"+(noticeContent == null ? "空" : noticeContent)+"，发布范围: 1全部学生 2指定班级 3指定专业:"+(targetType == null ? "空" : targetType)+"，发布目标ID:"+(targetIds == null ? "空" : targetIds)+"，状态: 0草稿 1已发布 2已下线:"+(status == null ? "空" : status)+"，是否置顶: 0否 1是:"+(isTop == null ? "空" : isTop)+"，发布时间:"+(publishTime == null ? "空" : DateUtil.format(publishTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，浏览次数:"+(viewCount == null ? "空" : viewCount)+"，创建人ID:"+(createUserId == null ? "空" : createUserId)+"，创建人姓名:"+(createUserName == null ? "空" : createUserName)+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，最后更新时间:"+(lastUpdateTime == null ? "空" : DateUtil.format(lastUpdateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
