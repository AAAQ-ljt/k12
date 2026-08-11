package com.nexora.entity.query;

import java.util.Date;


/**
 * 系统全局配置表参数
 */
public class SystemConfigQuery extends BaseParam {


	/**
	 * 配置ID
	 */
	private Integer configId;

	/**
	 * 分组：AI_MODEL/RAG/PYODIDE/SYSTEM/SECURITY
	 */
	private String configGroup;

	private String configGroupFuzzy;

	/**
	 * 配置键
	 */
	private String configKey;

	private String configKeyFuzzy;

	/**
	 * 配置值，支持字符串/JSON
	 */
	private String configValue;

	private String configValueFuzzy;

	/**
	 * 值类型：STRING/INT/FLOAT/BOOLEAN/JSON
	 */
	private String configType;

	private String configTypeFuzzy;

	/**
	 * 配置说明
	 */
	private String description;

	private String descriptionFuzzy;

	/**
	 * 状态：0停用 1启用
	 */
	private Integer status;

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


	public void setConfigId(Integer configId){
		this.configId = configId;
	}

	public Integer getConfigId(){
		return this.configId;
	}

	public void setConfigGroup(String configGroup){
		this.configGroup = configGroup;
	}

	public String getConfigGroup(){
		return this.configGroup;
	}

	public void setConfigGroupFuzzy(String configGroupFuzzy){
		this.configGroupFuzzy = configGroupFuzzy;
	}

	public String getConfigGroupFuzzy(){
		return this.configGroupFuzzy;
	}

	public void setConfigKey(String configKey){
		this.configKey = configKey;
	}

	public String getConfigKey(){
		return this.configKey;
	}

	public void setConfigKeyFuzzy(String configKeyFuzzy){
		this.configKeyFuzzy = configKeyFuzzy;
	}

	public String getConfigKeyFuzzy(){
		return this.configKeyFuzzy;
	}

	public void setConfigValue(String configValue){
		this.configValue = configValue;
	}

	public String getConfigValue(){
		return this.configValue;
	}

	public void setConfigValueFuzzy(String configValueFuzzy){
		this.configValueFuzzy = configValueFuzzy;
	}

	public String getConfigValueFuzzy(){
		return this.configValueFuzzy;
	}

	public void setConfigType(String configType){
		this.configType = configType;
	}

	public String getConfigType(){
		return this.configType;
	}

	public void setConfigTypeFuzzy(String configTypeFuzzy){
		this.configTypeFuzzy = configTypeFuzzy;
	}

	public String getConfigTypeFuzzy(){
		return this.configTypeFuzzy;
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

	public void setStatus(Integer status){
		this.status = status;
	}

	public Integer getStatus(){
		return this.status;
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
