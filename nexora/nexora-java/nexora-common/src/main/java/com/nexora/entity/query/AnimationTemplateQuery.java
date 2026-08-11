package com.nexora.entity.query;

import java.util.Date;


/**
 * 动画模板库参数
 */
public class AnimationTemplateQuery extends BaseParam {


	/**
	 * 模板ID
	 */
	private Integer templateId;

	/**
	 * 模板名称
	 */
	private String templateName;

	private String templateNameFuzzy;

	/**
	 * 动画类型，对应意图：EXPLAIN/CONCEPT/PROCESS等
	 */
	private String templateType;

	private String templateTypeFuzzy;

	/**
	 * 学段：PRIMARY_LOW/PRIMARY_HIGH/JUNIOR/SENIOR/ALL
	 */
	private String stage;

	private String stageFuzzy;

	/**
	 * 模板描述
	 */
	private String description;

	private String descriptionFuzzy;

	/**
	 * SVG模板JSON，含分步脚本结构
	 */
	private String templateContent;

	private String templateContentFuzzy;

	/**
	 * 预览图URL，可空
	 */
	private String previewUrl;

	private String previewUrlFuzzy;

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
	private String createTime;

	private String createTimeStart;

	private String createTimeEnd;

	/**
	 * 更新时间
	 */
	private String updateTime;

	private String updateTimeStart;

	private String updateTimeEnd;


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

	public void setTemplateNameFuzzy(String templateNameFuzzy){
		this.templateNameFuzzy = templateNameFuzzy;
	}

	public String getTemplateNameFuzzy(){
		return this.templateNameFuzzy;
	}

	public void setTemplateType(String templateType){
		this.templateType = templateType;
	}

	public String getTemplateType(){
		return this.templateType;
	}

	public void setTemplateTypeFuzzy(String templateTypeFuzzy){
		this.templateTypeFuzzy = templateTypeFuzzy;
	}

	public String getTemplateTypeFuzzy(){
		return this.templateTypeFuzzy;
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

	public void setTemplateContent(String templateContent){
		this.templateContent = templateContent;
	}

	public String getTemplateContent(){
		return this.templateContent;
	}

	public void setTemplateContentFuzzy(String templateContentFuzzy){
		this.templateContentFuzzy = templateContentFuzzy;
	}

	public String getTemplateContentFuzzy(){
		return this.templateContentFuzzy;
	}

	public void setPreviewUrl(String previewUrl){
		this.previewUrl = previewUrl;
	}

	public String getPreviewUrl(){
		return this.previewUrl;
	}

	public void setPreviewUrlFuzzy(String previewUrlFuzzy){
		this.previewUrlFuzzy = previewUrlFuzzy;
	}

	public String getPreviewUrlFuzzy(){
		return this.previewUrlFuzzy;
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
