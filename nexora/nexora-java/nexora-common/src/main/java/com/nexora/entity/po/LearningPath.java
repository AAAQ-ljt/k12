package com.nexora.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import com.nexora.entity.enums.DateTimePatternEnum;
import com.nexora.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 学习路径表
 */
public class LearningPath implements Serializable {


	/**
	 * 路径ID
	 */
	private String pathId;

	/**
	 * 学生
	 */
	private Integer userId;

	/**
	 * 学习分类/目标名（学生自建或AI命名）
	 */
	private String title;

	/**
	 * 学段【冗余快照】
	 */
	private String stage;

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

	public void setTitle(String title){
		this.title = title;
	}

	public String getTitle(){
		return this.title;
	}

	public void setStage(String stage){
		this.stage = stage;
	}

	public String getStage(){
		return this.stage;
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
		return "路径ID:"+(pathId == null ? "空" : pathId)+"，学生:"+(userId == null ? "空" : userId)+"，学习分类/目标名（学生自建或AI命名）:"+(title == null ? "空" : title)+"，学段【冗余快照】:"+(stage == null ? "空" : stage)+"，来源：0规则生成 1AI生成:"+(source == null ? "空" : source)+"，状态：0进行中 1已完成 2已放弃:"+(status == null ? "空" : status)+"，节点总数【冗余：节点增删时维护】:"+(totalItems == null ? "空" : totalItems)+"，已完成节点数【冗余】:"+(finishedItems == null ? "空" : finishedItems)+"，进度百分比【冗余：列表直读免聚合】:"+(progress == null ? "空" : progress)+"，当前节点，AI主动引导锚点:"+(currentItemId == null ? "空" : currentItemId)+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，更新时间:"+(updateTime == null ? "空" : DateUtil.format(updateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
