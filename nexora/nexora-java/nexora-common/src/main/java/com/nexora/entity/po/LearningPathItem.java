package com.nexora.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import com.nexora.entity.enums.DateTimePatternEnum;
import com.nexora.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 路径节点表
 */
public class LearningPathItem implements Serializable {


	/**
	 * 节点ID
	 */
	private String itemId;

	/**
	 * 所属路径
	 */
	private String pathId;

	/**
	 * 学生【冗余：到期复习直查免join路径表】
	 */
	private Integer userId;

	/**
	 * 知识点
	 */
	private String knowledgePointId;

	/**
	 * 知识点名【冗余快照】
	 */
	private String knowledgePointName;

	/**
	 * 0主线 1兴趣分支
	 */
	private Integer branchType;

	/**
	 * 分支名，可空
	 */
	private String branchName;

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
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date dueDate;

	/**
	 * 排序
	 */
	private Integer sort;

	/**
	 * 完成时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date finishTime;

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	/**
	 * 更新时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date updateTime;


	public void setItemId(String itemId){
		this.itemId = itemId;
	}

	public String getItemId(){
		return this.itemId;
	}

	public void setPathId(String pathId){
		this.pathId = pathId;
	}

	public String getPathId(){
		return this.pathId;
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

	public void setKnowledgePointName(String knowledgePointName){
		this.knowledgePointName = knowledgePointName;
	}

	public String getKnowledgePointName(){
		return this.knowledgePointName;
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

	public void setDueDate(Date dueDate){
		this.dueDate = dueDate;
	}

	public Date getDueDate(){
		return this.dueDate;
	}

	public void setSort(Integer sort){
		this.sort = sort;
	}

	public Integer getSort(){
		return this.sort;
	}

	public void setFinishTime(Date finishTime){
		this.finishTime = finishTime;
	}

	public Date getFinishTime(){
		return this.finishTime;
	}

	public void setCreateTime(Date createTime){
		this.createTime = createTime;
	}

	public Date getCreateTime(){
		return this.createTime;
	}

	public void setUpdateTime(Date updateTime){
		this.updateTime = updateTime;
	}

	public Date getUpdateTime(){
		return this.updateTime;
	}

	@Override
	public String toString (){
		return "节点ID:"+(itemId == null ? "空" : itemId)+"，所属路径:"+(pathId == null ? "空" : pathId)+"，学生【冗余：到期复习直查免join路径表】:"+(userId == null ? "空" : userId)+"，知识点:"+(knowledgePointId == null ? "空" : knowledgePointId)+"，知识点名【冗余快照】:"+(knowledgePointName == null ? "空" : knowledgePointName)+"，0主线 1兴趣分支:"+(branchType == null ? "空" : branchType)+"，分支名，可空:"+(branchName == null ? "空" : branchName)+"，0学习 1复习（遗忘曲线复习节点）:"+(itemType == null ? "空" : itemType)+"，状态：0未解锁 1进行中 2已掌握:"+(status == null ? "空" : status)+"，复习到期日（item_type=1时有效）:"+(dueDate == null ? "空" : DateUtil.format(dueDate, DateTimePatternEnum.YYYY_MM_DD.getPattern()))+"，排序:"+(sort == null ? "空" : sort)+"，完成时间:"+(finishTime == null ? "空" : DateUtil.format(finishTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，更新时间:"+(updateTime == null ? "空" : DateUtil.format(updateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
