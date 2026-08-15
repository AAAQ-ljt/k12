package com.nexora.entity.po;

import java.util.Date;
import com.nexora.entity.enums.DateTimePatternEnum;
import com.nexora.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 题目表
 */
public class QuestionInfo implements Serializable {


	/**
	 * 题目ID
	 */
	private String questionId;

	/**
	 * 知识点【冗余：出题/练习主筛选键】
	 */
	private String knowledgePointId;

	/**
	 * 学段【冗余：按学段抽题免join】
	 */
	private String stage;

	/**
	 * 年级【按年级抽题免join】
	 */
	private String grade;

	/**
	 * 难度：1-3
	 */
	private Integer difficulty;

	/**
	 * 题型：0单选 1多选 2判断 3填空 4简答 5解答 6论述 7材料分析
	 */
	private Integer questionType;

	/**
	 * 题干
	 */
	private String title;

	/**
	 * 题目配图，关联resource_info.resource_id，多个逗号分隔，可空
	 */
	private String questionImage;

	/**
	 * 判断/填空答案；选择题答案在选项表
	 */
	private String answer;

	/**
	 * 解析
	 */
	private String analysis;

	/**
	 * 来源：0管理员录入 1AI生成
	 */
	private Integer source;

	/**
	 * 审核：0待审核 1已上架 2已驳回
	 */
	private Integer auditStatus;

	/**
	 * 默认分值
	 */
	private Integer score;

	/**
	 * 状态：0停用 1启用
	 */
	private Integer status;

	/**
	 * 录入人，可空（AI生成为空）
	 */
	private Integer createBy;

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


	public void setQuestionId(String questionId){
		this.questionId = questionId;
	}

	public String getQuestionId(){
		return this.questionId;
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

	public void setGrade(String grade){
		this.grade = grade;
	}

	public String getGrade(){
		return this.grade;
	}

	public void setDifficulty(Integer difficulty){
		this.difficulty = difficulty;
	}

	public Integer getDifficulty(){
		return this.difficulty;
	}

	public void setQuestionType(Integer questionType){
		this.questionType = questionType;
	}

	public Integer getQuestionType(){
		return this.questionType;
	}

	public void setTitle(String title){
		this.title = title;
	}

	public String getTitle(){
		return this.title;
	}

	public void setQuestionImage(String questionImage){
		this.questionImage = questionImage;
	}

	public String getQuestionImage(){
		return this.questionImage;
	}

	public void setAnswer(String answer){
		this.answer = answer;
	}

	public String getAnswer(){
		return this.answer;
	}

	public void setAnalysis(String analysis){
		this.analysis = analysis;
	}

	public String getAnalysis(){
		return this.analysis;
	}

	public void setSource(Integer source){
		this.source = source;
	}

	public Integer getSource(){
		return this.source;
	}

	public void setAuditStatus(Integer auditStatus){
		this.auditStatus = auditStatus;
	}

	public Integer getAuditStatus(){
		return this.auditStatus;
	}

	public void setScore(Integer score){
		this.score = score;
	}

	public Integer getScore(){
		return this.score;
	}

	public void setStatus(Integer status){
		this.status = status;
	}

	public Integer getStatus(){
		return this.status;
	}

	public void setCreateBy(Integer createBy){
		this.createBy = createBy;
	}

	public Integer getCreateBy(){
		return this.createBy;
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
		return "题目ID:"+(questionId == null ? "空" : questionId)+"，知识点【冗余：出题/练习主筛选键】:"+(knowledgePointId == null ? "空" : knowledgePointId)+"，学段【冗余：按学段抽题免join】:"+(stage == null ? "空" : stage)+"，年级【按年级抽题免join】:"+(grade == null ? "空" : grade)+"，难度：1-3:"+(difficulty == null ? "空" : difficulty)+"，题型：0单选 1多选 2判断 3填空:"+(questionType == null ? "空" : questionType)+"，题干:"+(title == null ? "空" : title)+"，题目配图，关联resource_info.resource_id，多个逗号分隔，可空:"+(questionImage == null ? "空" : questionImage)+"，判断/填空答案；选择题答案在选项表:"+(answer == null ? "空" : answer)+"，解析:"+(analysis == null ? "空" : analysis)+"，来源：0管理员录入 1AI生成:"+(source == null ? "空" : source)+"，审核：0待审核 1已上架 2已驳回:"+(auditStatus == null ? "空" : auditStatus)+"，默认分值:"+(score == null ? "空" : score)+"，状态：0停用 1启用:"+(status == null ? "空" : status)+"，录入人，可空（AI生成为空）:"+(createBy == null ? "空" : createBy)+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，更新时间:"+(updateTime == null ? "空" : DateUtil.format(updateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
