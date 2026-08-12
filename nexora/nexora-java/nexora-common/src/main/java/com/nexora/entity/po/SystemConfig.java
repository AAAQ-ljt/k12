package com.nexora.entity.po;

import java.util.Date;
import com.nexora.entity.enums.DateTimePatternEnum;
import com.nexora.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 系统全局配置表
 */
public class SystemConfig implements Serializable {


	/**
	 * 配置ID
	 */
	private Integer configId;

	/**
	 * 分组：AI_MODEL/RAG/PYODIDE/SYSTEM/SECURITY
	 */
	private String configGroup;

	/**
	 * 配置键
	 */
	private String configKey;

	/**
	 * 配置值，支持字符串/JSON
	 */
	private String configValue;

	/**
	 * 值类型：STRING/INT/FLOAT/BOOLEAN/JSON
	 */
	private String configType;

	/**
	 * 配置说明
	 */
	private String description;

	/**
	 * 状态：0停用 1启用
	 */
	private Integer status;

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

	public void setConfigKey(String configKey){
		this.configKey = configKey;
	}

	public String getConfigKey(){
		return this.configKey;
	}

	public void setConfigValue(String configValue){
		this.configValue = configValue;
	}

	public String getConfigValue(){
		return this.configValue;
	}

	public void setConfigType(String configType){
		this.configType = configType;
	}

	public String getConfigType(){
		return this.configType;
	}

	public void setDescription(String description){
		this.description = description;
	}

	public String getDescription(){
		return this.description;
	}

	public void setStatus(Integer status){
		this.status = status;
	}

	public Integer getStatus(){
		return this.status;
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
		return "配置ID:"+(configId == null ? "空" : configId)+"，分组：AI_MODEL/RAG/PYODIDE/SYSTEM/SECURITY:"+(configGroup == null ? "空" : configGroup)+"，配置键:"+(configKey == null ? "空" : configKey)+"，配置值，支持字符串/JSON:"+(configValue == null ? "空" : configValue)+"，值类型：STRING/INT/FLOAT/BOOLEAN/JSON:"+(configType == null ? "空" : configType)+"，配置说明:"+(description == null ? "空" : description)+"，状态：0停用 1启用:"+(status == null ? "空" : status)+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，更新时间:"+(updateTime == null ? "空" : DateUtil.format(updateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
