package com.nexora.entity.query;

import java.util.Date;


/**
 * 知识点掌握度表参数
 */
public class KnowledgeMasteryQuery extends BaseParam {


	/**
	 * 主键
	 */
	private Integer id;

	/**
	 * 学生
	 */
	private String userId;

	/**
	 * 知识点
	 */
	private String knowledgePointId;

	private String knowledgePointIdFuzzy;

	/**
	 * 学段【冗余：雷达图按学段聚合免join】
	 */
	private String stage;

	private String stageFuzzy;

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
	private String lastPracticeTime;

	private String lastPracticeTimeStart;

	private String lastPracticeTimeEnd;

	/**
	 * 掌握时间（遗忘曲线计时起点）
	 */
	private String lastMasterTime;

	private String lastMasterTimeStart;

	private String lastMasterTimeEnd;

	/**
	 * 下次复习时间
	 */
	private String nextReviewTime;

	private String nextReviewTimeStart;

	private String nextReviewTimeEnd;

	/**
	 * 遗忘曲线阶段0-4（对应1/3/7/15天间隔）
	 */
	private Integer reviewStage;

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


	public void setId(Integer id){
		this.id = id;
	}

	public Integer getId(){
		return this.id;
	}

	public void setUserId(String userId){
		this.userId = userId;
	}

	public String getUserId(){
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

	public void setLastPracticeTime(String lastPracticeTime){
		this.lastPracticeTime = lastPracticeTime;
	}

	public String getLastPracticeTime(){
		return this.lastPracticeTime;
	}

	public void setLastPracticeTimeStart(String lastPracticeTimeStart){
		this.lastPracticeTimeStart = lastPracticeTimeStart;
	}

	public String getLastPracticeTimeStart(){
		return this.lastPracticeTimeStart;
	}
	public void setLastPracticeTimeEnd(String lastPracticeTimeEnd){
		this.lastPracticeTimeEnd = lastPracticeTimeEnd;
	}

	public String getLastPracticeTimeEnd(){
		return this.lastPracticeTimeEnd;
	}

	public void setLastMasterTime(String lastMasterTime){
		this.lastMasterTime = lastMasterTime;
	}

	public String getLastMasterTime(){
		return this.lastMasterTime;
	}

	public void setLastMasterTimeStart(String lastMasterTimeStart){
		this.lastMasterTimeStart = lastMasterTimeStart;
	}

	public String getLastMasterTimeStart(){
		return this.lastMasterTimeStart;
	}
	public void setLastMasterTimeEnd(String lastMasterTimeEnd){
		this.lastMasterTimeEnd = lastMasterTimeEnd;
	}

	public String getLastMasterTimeEnd(){
		return this.lastMasterTimeEnd;
	}

	public void setNextReviewTime(String nextReviewTime){
		this.nextReviewTime = nextReviewTime;
	}

	public String getNextReviewTime(){
		return this.nextReviewTime;
	}

	public void setNextReviewTimeStart(String nextReviewTimeStart){
		this.nextReviewTimeStart = nextReviewTimeStart;
	}

	public String getNextReviewTimeStart(){
		return this.nextReviewTimeStart;
	}
	public void setNextReviewTimeEnd(String nextReviewTimeEnd){
		this.nextReviewTimeEnd = nextReviewTimeEnd;
	}

	public String getNextReviewTimeEnd(){
		return this.nextReviewTimeEnd;
	}

	public void setReviewStage(Integer reviewStage){
		this.reviewStage = reviewStage;
	}

	public Integer getReviewStage(){
		return this.reviewStage;
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
