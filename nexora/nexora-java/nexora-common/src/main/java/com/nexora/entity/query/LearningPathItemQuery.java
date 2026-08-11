package com.nexora.entity.query;

import java.util.Date;


/**
 * 路径节点表参数
 */
public class LearningPathItemQuery extends BaseParam {


	/**
	 * 节点ID
	 */
	private String itemId;

	private String itemIdFuzzy;

	/**
	 * 所属路径
	 */
	private String pathId;

	private String pathIdFuzzy;

	/**
	 * 学生【冗余：到期复习直查免join路径表】
	 */
	private Integer userId;

	/**
	 * 知识点
	 */
	private String knowledgePointId;

	private String knowledgePointIdFuzzy;

	/**
	 * 知识点名【冗余快照】
	 */
	private String knowledgePointName;

	private String knowledgePointNameFuzzy;

	/**
	 * 0主线 1兴趣分支
	 */
	private Integer branchType;

	/**
	 * 分支名，可空
	 */
	private String branchName;

	private String branchNameFuzzy;

	/**
	 * 0学习 1复习（遗忘曲线复习节点）
	 */
	private Integer itemType;

	/**
	 * 状态：0未解锁 1进行中 2已掌握
	 */
	private Integer status;

	/**
	 * 复习到期日（item_type=1时有效）
	 */
	private String dueDate;

	private String dueDateStart;

	private String dueDateEnd;

	/**
	 * 排序
	 */
	private Integer sort;

	/**
	 * 完成时间
	 */
	private String finishTime;

	private String finishTimeStart;

	private String finishTimeEnd;

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


	public void setItemId(String itemId){
		this.itemId = itemId;
	}

	public String getItemId(){
		return this.itemId;
	}

	public void setItemIdFuzzy(String itemIdFuzzy){
		this.itemIdFuzzy = itemIdFuzzy;
	}

	public String getItemIdFuzzy(){
		return this.itemIdFuzzy;
	}

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

	public void setUserId(Integer userId){
		this.userId = userId;
	}

	public Integer getUserId(){
		return this.userId;
	}

	public void setKnowledgePointId(String knowledgePointId){
		this.knowledgePointId = knowledgePointId;
	}

	public String getKnowledgePointId(){
		return this.knowledgePointId;
	}

	public void setKnowledgePointIdFuzzy(String knowledgePointIdFuzzy){
		this.knowledgePointIdFuzzy = knowledgePointIdFuzzy;
	}

	public String getKnowledgePointIdFuzzy(){
		return this.knowledgePointIdFuzzy;
	}

	public void setKnowledgePointName(String knowledgePointName){
		this.knowledgePointName = knowledgePointName;
	}

	public String getKnowledgePointName(){
		return this.knowledgePointName;
	}

	public void setKnowledgePointNameFuzzy(String knowledgePointNameFuzzy){
		this.knowledgePointNameFuzzy = knowledgePointNameFuzzy;
	}

	public String getKnowledgePointNameFuzzy(){
		return this.knowledgePointNameFuzzy;
	}

	public void setBranchType(Integer branchType){
		this.branchType = branchType;
	}

	public Integer getBranchType(){
		return this.branchType;
	}

	public void setBranchName(String branchName){
		this.branchName = branchName;
	}

	public String getBranchName(){
		return this.branchName;
	}

	public void setBranchNameFuzzy(String branchNameFuzzy){
		this.branchNameFuzzy = branchNameFuzzy;
	}

	public String getBranchNameFuzzy(){
		return this.branchNameFuzzy;
	}

	public void setItemType(Integer itemType){
		this.itemType = itemType;
	}

	public Integer getItemType(){
		return this.itemType;
	}

	public void setStatus(Integer status){
		this.status = status;
	}

	public Integer getStatus(){
		return this.status;
	}

	public void setDueDate(String dueDate){
		this.dueDate = dueDate;
	}

	public String getDueDate(){
		return this.dueDate;
	}

	public void setDueDateStart(String dueDateStart){
		this.dueDateStart = dueDateStart;
	}

	public String getDueDateStart(){
		return this.dueDateStart;
	}
	public void setDueDateEnd(String dueDateEnd){
		this.dueDateEnd = dueDateEnd;
	}

	public String getDueDateEnd(){
		return this.dueDateEnd;
	}

	public void setSort(Integer sort){
		this.sort = sort;
	}

	public Integer getSort(){
		return this.sort;
	}

	public void setFinishTime(String finishTime){
		this.finishTime = finishTime;
	}

	public String getFinishTime(){
		return this.finishTime;
	}

	public void setFinishTimeStart(String finishTimeStart){
		this.finishTimeStart = finishTimeStart;
	}

	public String getFinishTimeStart(){
		return this.finishTimeStart;
	}
	public void setFinishTimeEnd(String finishTimeEnd){
		this.finishTimeEnd = finishTimeEnd;
	}

	public String getFinishTimeEnd(){
		return this.finishTimeEnd;
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
