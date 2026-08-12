package com.nexora.entity.po;

import java.util.Date;
import com.nexora.entity.enums.DateTimePatternEnum;
import com.nexora.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 知识点表（领域中心）
 */
public class KnowledgePoint implements Serializable {


	/**
	 * 知识点ID
	 */
	private String knowledgePointId;

	/**
	 * 知识点名；同名跨学段多行，(name, stage)逻辑唯一
	 */
	private String name;

	/**
	 * 学段
	 */
	private String stage;

	/**
	 * 学科
	 */
	private String subject;

	/**
	 * 难度：1-3
	 */
	private Integer difficulty;

	/**
	 * 描述
	 */
	private String description;

	/**
	 * 封面，可空
	 */
	private String cover;

	/**
	 * 关联课时，可空
	 */
	private String lessonId;

	/**
	 * 排序
	 */
	private Integer sort;

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


	public void setKnowledgePointId(String knowledgePointId){
		this.knowledgePointId = knowledgePointId;
	}

	public String getKnowledgePointId(){
		return this.knowledgePointId;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return this.name;
	}

	public void setStage(String stage){
		this.stage = stage;
	}

	public String getStage(){
		return this.stage;
	}

	public void setSubject(String subject){
		this.subject = subject;
	}

	public String getSubject(){
		return this.subject;
	}

	public void setDifficulty(Integer difficulty){
		this.difficulty = difficulty;
	}

	public Integer getDifficulty(){
		return this.difficulty;
	}

	public void setDescription(String description){
		this.description = description;
	}

	public String getDescription(){
		return this.description;
	}

	public void setCover(String cover){
		this.cover = cover;
	}

	public String getCover(){
		return this.cover;
	}

	public void setLessonId(String lessonId){
		this.lessonId = lessonId;
	}

	public String getLessonId(){
		return this.lessonId;
	}

	public void setSort(Integer sort){
		this.sort = sort;
	}

	public Integer getSort(){
		return this.sort;
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
		return "知识点ID:"+(knowledgePointId == null ? "空" : knowledgePointId)+"，知识点名；同名跨学段多行，(name, stage)逻辑唯一:"+(name == null ? "空" : name)+"，学段:"+(stage == null ? "空" : stage)+"，学科:"+(subject == null ? "空" : subject)+"，难度：1-3:"+(difficulty == null ? "空" : difficulty)+"，描述:"+(description == null ? "空" : description)+"，封面，可空:"+(cover == null ? "空" : cover)+"，关联课时，可空:"+(lessonId == null ? "空" : lessonId)+"，排序:"+(sort == null ? "空" : sort)+"，状态：0停用 1启用:"+(status == null ? "空" : status)+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，更新时间:"+(updateTime == null ? "空" : DateUtil.format(updateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
