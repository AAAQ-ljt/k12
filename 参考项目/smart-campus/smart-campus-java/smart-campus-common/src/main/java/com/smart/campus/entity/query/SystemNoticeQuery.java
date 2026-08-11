package com.smart.campus.entity.query;

import java.util.Date;


/**
 * 系统通知公告表参数
 */
public class SystemNoticeQuery extends BaseParam {


	/**
	 * 通知ID
	 */
	private String noticeId;

	private String noticeIdFuzzy;

	/**
	 * 通知标题
	 */
	private String noticeTitle;

	private String noticeTitleFuzzy;

	/**
	 * 通知内容
	 */
	private String noticeContent;

	private String noticeContentFuzzy;

	/**
	 * 发布范围: 1全部学生 2指定班级 3指定专业
	 */
	private Integer targetType;

	/**
	 * 发布目标ID，多个用英文逗号分隔
	 */
	private String targetIds;

	private String targetIdsFuzzy;

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
	private String publishTime;

	private String publishTimeStart;

	private String publishTimeEnd;

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

	private String createUserNameFuzzy;

	/**
	 * 创建时间
	 */
	private String createTime;

	private String createTimeStart;

	private String createTimeEnd;

	/**
	 * 最后更新时间
	 */
	private String lastUpdateTime;

	private String lastUpdateTimeStart;

	private String lastUpdateTimeEnd;


	public void setNoticeId(String noticeId){
		this.noticeId = noticeId;
	}

	public String getNoticeId(){
		return this.noticeId;
	}

	public void setNoticeIdFuzzy(String noticeIdFuzzy){
		this.noticeIdFuzzy = noticeIdFuzzy;
	}

	public String getNoticeIdFuzzy(){
		return this.noticeIdFuzzy;
	}

	public void setNoticeTitle(String noticeTitle){
		this.noticeTitle = noticeTitle;
	}

	public String getNoticeTitle(){
		return this.noticeTitle;
	}

	public void setNoticeTitleFuzzy(String noticeTitleFuzzy){
		this.noticeTitleFuzzy = noticeTitleFuzzy;
	}

	public String getNoticeTitleFuzzy(){
		return this.noticeTitleFuzzy;
	}

	public void setNoticeContent(String noticeContent){
		this.noticeContent = noticeContent;
	}

	public String getNoticeContent(){
		return this.noticeContent;
	}

	public void setNoticeContentFuzzy(String noticeContentFuzzy){
		this.noticeContentFuzzy = noticeContentFuzzy;
	}

	public String getNoticeContentFuzzy(){
		return this.noticeContentFuzzy;
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

	public void setTargetIdsFuzzy(String targetIdsFuzzy){
		this.targetIdsFuzzy = targetIdsFuzzy;
	}

	public String getTargetIdsFuzzy(){
		return this.targetIdsFuzzy;
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

	public void setCreateUserNameFuzzy(String createUserNameFuzzy){
		this.createUserNameFuzzy = createUserNameFuzzy;
	}

	public String getCreateUserNameFuzzy(){
		return this.createUserNameFuzzy;
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

	public void setLastUpdateTime(String lastUpdateTime){
		this.lastUpdateTime = lastUpdateTime;
	}

	public String getLastUpdateTime(){
		return this.lastUpdateTime;
	}

	public void setLastUpdateTimeStart(String lastUpdateTimeStart){
		this.lastUpdateTimeStart = lastUpdateTimeStart;
	}

	public String getLastUpdateTimeStart(){
		return this.lastUpdateTimeStart;
	}
	public void setLastUpdateTimeEnd(String lastUpdateTimeEnd){
		this.lastUpdateTimeEnd = lastUpdateTimeEnd;
	}

	public String getLastUpdateTimeEnd(){
		return this.lastUpdateTimeEnd;
	}

}
