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
 * 试卷信息表
 */
public class PaperInfo implements Serializable {


	/**
	 * 主键ID
	 */
	private String paperId;

	/**
	 * 试卷名称
	 */
	private String paperName;

	/**
	 * 试卷类型:1课后习题 2考试试卷
	 */
	private Integer paperType;

	/**
	 * 试卷说明
	 */
	private String description;

	/**
	 * 试卷总分
	 */
	private BigDecimal totalScore;

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


	public void setPaperId(String paperId){
		this.paperId = paperId;
	}

	public String getPaperId(){
		return this.paperId;
	}

	public void setPaperName(String paperName){
		this.paperName = paperName;
	}

	public String getPaperName(){
		return this.paperName;
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

	public void setTotalScore(BigDecimal totalScore){
		this.totalScore = totalScore;
	}

	public BigDecimal getTotalScore(){
		return this.totalScore;
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
		return "主键ID:"+(paperId == null ? "空" : paperId)+"，试卷名称:"+(paperName == null ? "空" : paperName)+"，试卷类型:1课后习题 2考试试卷:"+(paperType == null ? "空" : paperType)+"，试卷说明:"+(description == null ? "空" : description)+"，试卷总分:"+(totalScore == null ? "空" : totalScore)+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，更新时间:"+(updateTime == null ? "空" : DateUtil.format(updateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
