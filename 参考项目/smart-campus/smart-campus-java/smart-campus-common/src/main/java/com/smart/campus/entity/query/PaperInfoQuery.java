package com.smart.campus.entity.query;

import java.math.BigDecimal;
import java.util.Date;


/**
 * 试卷信息表参数
 */
public class PaperInfoQuery extends BaseParam {


	/**
	 * 主键ID
	 */
	private String paperId;

	private String paperIdFuzzy;

	/**
	 * 试卷名称
	 */
	private String paperName;

	private String paperNameFuzzy;

	/**
	 * 试卷类型:1课后习题 2考试试卷
	 */
	private Integer paperType;

	/**
	 * 试卷说明
	 */
	private String description;

	private String descriptionFuzzy;

	/**
	 * 试卷总分
	 */
	private BigDecimal totalScore;

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


	public void setPaperId(String paperId){
		this.paperId = paperId;
	}

	public String getPaperId(){
		return this.paperId;
	}

	public void setPaperIdFuzzy(String paperIdFuzzy){
		this.paperIdFuzzy = paperIdFuzzy;
	}

	public String getPaperIdFuzzy(){
		return this.paperIdFuzzy;
	}

	public void setPaperName(String paperName){
		this.paperName = paperName;
	}

	public String getPaperName(){
		return this.paperName;
	}

	public void setPaperNameFuzzy(String paperNameFuzzy){
		this.paperNameFuzzy = paperNameFuzzy;
	}

	public String getPaperNameFuzzy(){
		return this.paperNameFuzzy;
	}

	public void setPaperType(Integer paperType){
		this.paperType = paperType;
	}

	public Integer getPaperType(){
		return this.paperType;
	}

	public void setDescription(String description){
		this.description = description;
	}

	public String getDescription(){
		return this.description;
	}

	public void setDescriptionFuzzy(String descriptionFuzzy){
		this.descriptionFuzzy = descriptionFuzzy;
	}

	public String getDescriptionFuzzy(){
		return this.descriptionFuzzy;
	}

	public void setTotalScore(BigDecimal totalScore){
		this.totalScore = totalScore;
	}

	public BigDecimal getTotalScore(){
		return this.totalScore;
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
