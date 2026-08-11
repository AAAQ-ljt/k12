package com.nexora.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import com.nexora.entity.enums.DateTimePatternEnum;
import com.nexora.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 提示词模板表
 */
public class PromptTemplate implements Serializable {


	/**
	 * 主键
	 */
	private Integer id;

	/**
	 * 学段；ALL表示通用模板
	 */
	private String stage;

	/**
	 * 场景/意图：EXPLAIN/QUIZ/PICTURE_BOOK/DRAW/ANIMATION/CODING/PLAN/PROGRESS/CHAT等
	 */
	private String scene;

	/**
	 * 模板名
	 */
	private String templateName;

	/**
	 * 提示词内容（必须含"知识库无相关内容时如实说明，不要编造"类约束）
	 */
	private String content;

	/**
	 * 状态：0停用 1启用
	 */
	private Integer status;

	/**
	 * 备注
	 */
	private String remark;

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

	public void setStage(String stage){
		this.stage = stage;
	}

	public String getStage(){
		return this.stage;
	}

	public void setScene(String scene){
		this.scene = scene;
	}

	public String getScene(){
		return this.scene;
	}

	public void setTemplateName(String templateName){
		this.templateName = templateName;
	}

	public String getTemplateName(){
		return this.templateName;
	}

	public void setContent(String content){
		this.content = content;
	}

	public String getContent(){
		return this.content;
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
		return "主键:"+(id == null ? "空" : id)+"，学段；ALL表示通用模板:"+(stage == null ? "空" : stage)+"，场景/意图：EXPLAIN/QUIZ/PICTURE_BOOK/DRAW/ANIMATION/CODING/PLAN/PROGRESS/CHAT等:"+(scene == null ? "空" : scene)+"，模板名:"+(templateName == null ? "空" : templateName)+"，提示词内容（必须含\"知识库无相关内容时如实说明，不要编造\"类约束）:"+(content == null ? "空" : content)+"，状态：0停用 1启用:"+(status == null ? "空" : status)+"，备注:"+(remark == null ? "空" : remark)+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，更新时间:"+(updateTime == null ? "空" : DateUtil.format(updateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
