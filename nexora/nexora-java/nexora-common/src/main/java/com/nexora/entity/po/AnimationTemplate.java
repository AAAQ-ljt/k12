package com.nexora.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import com.nexora.entity.enums.DateTimePatternEnum;
import com.nexora.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 动画模板库
 */
public class AnimationTemplate implements Serializable {


	/**
	 * 模板ID
	 */
	private Integer templateId;

	/**
	 * 模板名称
	 */
	private String templateName;

	/**
	 * 动画类型，对应意图：EXPLAIN/CONCEPT/PROCESS等
	 */
	private String templateType;

	/**
	 * 学段：PRIMARY_LOW/PRIMARY_HIGH/JUNIOR/SENIOR/ALL
	 */
	private String stage;

	/**
	 * 模板描述
	 */
	private String description;

	/**
	 * SVG模板JSON，含分步脚本结构
	 */
	private String templateContent;

	/**
	 * 预览图URL，可空
	 */
	private String previewUrl;

	/**
	 * 状态：0停用 1启用
	 */
	private Integer status;

	/**
	 * 创建人（管理员ID）
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


	public void setTemplateId(Integer templateId){
		this.templateId = templateId;
	}

	public Integer getTemplateId(){
		return this.templateId;
	}

	public void setTemplateName(String templateName){
		this.templateName = templateName;
	}

	public String getTemplateName(){
		return this.templateName;
	}

	public void setTemplateType(String templateType){
		this.templateType = templateType;
	}

	public String getTemplateType(){
		return this.templateType;
	}

	public void setStage(String stage){
		this.stage = stage;
	}

	public String getStage(){
		return this.stage;
	}

	public void setDescription(String description){
		this.description = description;
	}

	public String getDescription(){
		return this.description;
	}

	public void setTemplateContent(String templateContent){
		this.templateContent = templateContent;
	}

	public String getTemplateContent(){
		return this.templateContent;
	}

	public void setPreviewUrl(String previewUrl){
		this.previewUrl = previewUrl;
	}

	public String getPreviewUrl(){
		return this.previewUrl;
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
		return "模板ID:"+(templateId == null ? "空" : templateId)+"，模板名称:"+(templateName == null ? "空" : templateName)+"，动画类型，对应意图：EXPLAIN/CONCEPT/PROCESS等:"+(templateType == null ? "空" : templateType)+"，学段：PRIMARY_LOW/PRIMARY_HIGH/JUNIOR/SENIOR/ALL:"+(stage == null ? "空" : stage)+"，模板描述:"+(description == null ? "空" : description)+"，SVG模板JSON，含分步脚本结构:"+(templateContent == null ? "空" : templateContent)+"，预览图URL，可空:"+(previewUrl == null ? "空" : previewUrl)+"，状态：0停用 1启用:"+(status == null ? "空" : status)+"，创建人（管理员ID）:"+(createBy == null ? "空" : createBy)+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，更新时间:"+(updateTime == null ? "空" : DateUtil.format(updateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
