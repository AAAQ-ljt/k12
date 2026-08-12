package com.nexora.entity.query;

import java.util.Date;


/**
 * 学习路径表参数
 */
public class LearningPathQuery extends BaseParam {


	/**
	 * 路径ID
	 */
	private String pathId;

	private String pathIdFuzzy;

	/**
	 * 学生
	 */
	private String userId;

	/**
	 * 学习分类/目标名（学生自建或AI命名）
	 */
	private String title;

	private String titleFuzzy;

	/**
	 * 学段【冗余快照】
	 */
	private String stage;

	private String stageFuzzy;

	/**
	 * 来源：0规则生成 1AI生成
	 */
	private Integer source;

	/**
	 * 状态：0进行中 1已完成 2已放弃
	 */
	private Integer status;

	/**
	 * 节点总数【冗余：节点增删时维护】
	 */
	private Integer totalItems;

	/**
	 * 已完成节点数【冗余】
	 */
	private Integer finishedItems;

	/**
	 * 进度百分比【冗余：列表直读免聚合】
	 */
	private Integer progress;

	/**
	 * 当前节点，AI主动引导锚点
	 */
	private String currentItemId;

	private String currentItemIdFuzzy;

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


	public void setPathId(String pathId){
		this.pathId = pathId;
	}

	public String getPathId(){
		return this.pathId;
	}

	public void setPathIdFuzzy(String pathIdFuzzy){
		this.pathIdFuzzy = pathIdFuzzy;
	}

	public String getPathIdFuzzy(){
		return this.pathIdFuzzy;
	}

	public void setUserId(String userId){
		this.userId = userId;
	}

	public String getUserId(){
		return this.userId;
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

	public void setStage(String stage){
		this.stage = stage;
	}

	public String getStage(){
		return this.stage;
	}

	public void setStageFuzzy(String stageFuzzy){
		this.stageFuzzy = stageFuzzy;
	}

	public String getStageFuzzy(){
		return this.stageFuzzy;
	}

	public void setSource(Integer source){
		this.source = source;
	}

	public Integer getSource(){
		return this.source;
	}

	public void setStatus(Integer status){
		this.status = status;
	}

	public Integer getStatus(){
		return this.status;
	}

	public void setTotalItems(Integer totalItems){
		this.totalItems = totalItems;
	}

	public Integer getTotalItems(){
		return this.totalItems;
	}

	public void setFinishedItems(Integer finishedItems){
		this.finishedItems = finishedItems;
	}

	public Integer getFinishedItems(){
		return this.finishedItems;
	}

	public void setProgress(Integer progress){
		this.progress = progress;
	}

	public Integer getProgress(){
		return this.progress;
	}

	public void setCurrentItemId(String currentItemId){
		this.currentItemId = currentItemId;
	}

	public String getCurrentItemId(){
		return this.currentItemId;
	}

	public void setCurrentItemIdFuzzy(String currentItemIdFuzzy){
		this.currentItemIdFuzzy = currentItemIdFuzzy;
	}

	public String getCurrentItemIdFuzzy(){
		return this.currentItemIdFuzzy;
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
