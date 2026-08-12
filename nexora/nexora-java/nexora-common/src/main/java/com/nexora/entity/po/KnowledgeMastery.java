package com.nexora.entity.po;

import java.util.Date;
import com.nexora.entity.enums.DateTimePatternEnum;
import com.nexora.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 知识点掌握度表
 */
public class KnowledgeMastery implements Serializable {


	/**
	 * 主键
	 */
	private Integer id;

	/**
	 * 学生
	 */
	private Integer userId;

	/**
	 * 知识点
	 */
	private String knowledgePointId;

	/**
	 * 学段【冗余：雷达图按学段聚合免join】
	 */
	private String stage;

	/**
	 * 掌握度0-100
	 */
	private Integer masteryScore;

	/**
	 * 状态：0未解锁 1进行中 2已掌握
	 */
	private Integer status;

	/**
	 * 练习次数【冗余计数：批改后原子+1】
	 */
	private Integer practiceCount;

	/**
	 * 答对次数【冗余：正确率=correct_count/practice_count】
	 */
	private Integer correctCount;

	/**
	 * 最近练习时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lastPracticeTime;

	/**
	 * 掌握时间（遗忘曲线计时起点）
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lastMasterTime;

	/**
	 * 下次复习时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date nextReviewTime;

	/**
	 * 遗忘曲线阶段0-4（对应1/3/7/15天间隔）
	 */
	private Integer reviewStage;

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


	public void setId(Integer id){
		this.id = id;
	}

	public Integer getId(){
		return this.id;
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

	public void setStage(String stage){
		this.stage = stage;
	}

	public String getStage(){
		return this.stage;
	}

	public void setMasteryScore(Integer masteryScore){
		this.masteryScore = masteryScore;
	}

	public Integer getMasteryScore(){
		return this.masteryScore;
	}

	public void setStatus(Integer status){
		this.status = status;
	}

	public Integer getStatus(){
		return this.status;
	}

	public void setPracticeCount(Integer practiceCount){
		this.practiceCount = practiceCount;
	}

	public Integer getPracticeCount(){
		return this.practiceCount;
	}

	public void setCorrectCount(Integer correctCount){
		this.correctCount = correctCount;
	}

	public Integer getCorrectCount(){
		return this.correctCount;
	}

	public void setLastPracticeTime(Date lastPracticeTime){
		this.lastPracticeTime = lastPracticeTime;
	}

	public Date getLastPracticeTime(){
		return this.lastPracticeTime;
	}

	public void setLastMasterTime(Date lastMasterTime){
		this.lastMasterTime = lastMasterTime;
	}

	public Date getLastMasterTime(){
		return this.lastMasterTime;
	}

	public void setNextReviewTime(Date nextReviewTime){
		this.nextReviewTime = nextReviewTime;
	}

	public Date getNextReviewTime(){
		return this.nextReviewTime;
	}

	public void setReviewStage(Integer reviewStage){
		this.reviewStage = reviewStage;
	}

	public Integer getReviewStage(){
		return this.reviewStage;
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
		return "主键:"+(id == null ? "空" : id)+"，学生:"+(userId == null ? "空" : userId)+"，知识点:"+(knowledgePointId == null ? "空" : knowledgePointId)+"，学段【冗余：雷达图按学段聚合免join】:"+(stage == null ? "空" : stage)+"，掌握度0-100:"+(masteryScore == null ? "空" : masteryScore)+"，状态：0未解锁 1进行中 2已掌握:"+(status == null ? "空" : status)+"，练习次数【冗余计数：批改后原子+1】:"+(practiceCount == null ? "空" : practiceCount)+"，答对次数【冗余：正确率=correct_count/practice_count】:"+(correctCount == null ? "空" : correctCount)+"，最近练习时间:"+(lastPracticeTime == null ? "空" : DateUtil.format(lastPracticeTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，掌握时间（遗忘曲线计时起点）:"+(lastMasterTime == null ? "空" : DateUtil.format(lastMasterTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，下次复习时间:"+(nextReviewTime == null ? "空" : DateUtil.format(nextReviewTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，遗忘曲线阶段0-4（对应1/3/7/15天间隔）:"+(reviewStage == null ? "空" : reviewStage)+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，更新时间:"+(updateTime == null ? "空" : DateUtil.format(updateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
