package com.nexora.entity.query;

import java.util.Date;


/**
 * 提示词模板表参数
 */
public class PromptTemplateQuery extends BaseParam {


	/**
	 * 主键
	 */
	private Integer id;

	/**
	 * 学段；ALL表示通用模板
	 */
	private String stage;

	private String stageFuzzy;

	/**
	 * 场景/意图：EXPLAIN/QUIZ/PICTURE_BOOK/DRAW/ANIMATION/CODING/PLAN/PROGRESS/CHAT等
	 */
	private String scene;

	private String sceneFuzzy;

	/**
	 * 模板名
	 */
	private String templateName;

	private String templateNameFuzzy;

	/**
	 * 提示词内容（必须含"知识库无相关内容时如实说明，不要编造"类约束）
	 */
	private String content;

	private String contentFuzzy;

	/**
	 * 状态：0停用 1启用
	 */
	private Integer status;

	/**
	 * 备注
	 */
	private String remark;

	private String remarkFuzzy;

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

	public void setScene(String scene){
		this.scene = scene;
	}

	public String getScene(){
		return this.scene;
	}

	public void setSceneFuzzy(String sceneFuzzy){
		this.sceneFuzzy = sceneFuzzy;
	}

	public String getSceneFuzzy(){
		return this.sceneFuzzy;
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

	public void setContent(String content){
		this.content = content;
	}

	public String getContent(){
		return this.content;
	}

	public void setContentFuzzy(String contentFuzzy){
		this.contentFuzzy = contentFuzzy;
	}

	public String getContentFuzzy(){
		return this.contentFuzzy;
	}

	public void setStatus(Integer status){
		this.status = status;
	}

	public Integer getStatus(){
		return this.status;
	}

	public void setRemark(String remark){
		this.remark = remark;
	}

	public String getRemark(){
		return this.remark;
	}

	public void setRemarkFuzzy(String remarkFuzzy){
		this.remarkFuzzy = remarkFuzzy;
	}

	public String getRemarkFuzzy(){
		return this.remarkFuzzy;
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
