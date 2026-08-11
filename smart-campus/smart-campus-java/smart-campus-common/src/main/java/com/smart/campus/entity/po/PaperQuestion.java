package com.smart.campus.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.Date;
import com.smart.campus.entity.enums.DateTimePatternEnum;
import com.smart.campus.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 试卷题目编排表
 */
public class PaperQuestion implements Serializable {


	/**
	 * 主键ID
	 */
	private Integer id;

	/**
	 * 试卷ID，对应paper_info.paper_id
	 */
	private String paperId;

	/**
	 * 题目ID，对应question_info.question_id
	 */
	private Integer questionId;

	/**
	 * 试卷内题目分值
	 */
	private BigDecimal questionScore;

	/**
	 * 题目类型:1单选 2多选 3判断 4填空
	 */
	private Integer questionType;

	/**
	 * 分组 类型 1:分组 0:题目 
	 */
	private Integer sectionType;

	/**
	 * 分组名称，可为空，如单选题、多选题
	 */
	private String sectionName;

	/**
	 * 父ID,如果是分组父级ID为0
	 */
	private Integer parentId;

	/**
	 * 试卷内排序值
	 */
	private Integer sortOrder;

	/**
	 * 题目快照，建议存JSON，包含标题、配图、选项、答案、答案解析等
	 */
	private String questionSnapshot;

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

	public void setPaperId(String paperId){
		this.paperId = paperId;
	}

	public String getPaperId(){
		return this.paperId;
	}

	public void setQuestionId(Integer questionId){
		this.questionId = questionId;
	}

	public Integer getQuestionId(){
		return this.questionId;
	}

	public void setQuestionScore(BigDecimal questionScore){
		this.questionScore = questionScore;
	}

	public BigDecimal getQuestionScore(){
		return this.questionScore;
	}

	public void setQuestionType(Integer questionType){
		this.questionType = questionType;
	}

	public Integer getQuestionType(){
		return this.questionType;
	}

	public void setSectionType(Integer sectionType){
		this.sectionType = sectionType;
	}

	public Integer getSectionType(){
		return this.sectionType;
	}

	public void setSectionName(String sectionName){
		this.sectionName = sectionName;
	}

	public String getSectionName(){
		return this.sectionName;
	}

	public void setParentId(Integer parentId){
		this.parentId = parentId;
	}

	public Integer getParentId(){
		return this.parentId;
	}

	public void setSortOrder(Integer sortOrder){
		this.sortOrder = sortOrder;
	}

	public Integer getSortOrder(){
		return this.sortOrder;
	}

	public void setQuestionSnapshot(String questionSnapshot){
		this.questionSnapshot = questionSnapshot;
	}

	public String getQuestionSnapshot(){
		return this.questionSnapshot;
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
		return "主键ID:"+(id == null ? "空" : id)+"，试卷ID，对应paper_info.paper_id:"+(paperId == null ? "空" : paperId)+"，题目ID，对应question_info.question_id:"+(questionId == null ? "空" : questionId)+"，试卷内题目分值:"+(questionScore == null ? "空" : questionScore)+"，题目类型:1单选 2多选 3判断 4填空:"+(questionType == null ? "空" : questionType)+"，分组 类型 1:分组 0:题目 :"+(sectionType == null ? "空" : sectionType)+"，分组名称，可为空，如单选题、多选题:"+(sectionName == null ? "空" : sectionName)+"，父ID,如果是分组父级ID为0:"+(parentId == null ? "空" : parentId)+"，试卷内排序值:"+(sortOrder == null ? "空" : sortOrder)+"，题目快照，建议存JSON，包含标题、配图、选项、答案、答案解析等:"+(questionSnapshot == null ? "空" : questionSnapshot)+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，更新时间:"+(updateTime == null ? "空" : DateUtil.format(updateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
