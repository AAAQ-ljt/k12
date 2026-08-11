package com.smart.campus.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;


/**
 * 课时资源关联表
 */
public class CourseChapterLessonResource implements Serializable {


	/**
	 * 主键ID
	 */
	private Integer id;

	/**
	 * 课时ID
	 */
	private String lessonId;

	/**
	 * 资源ID，对应resource_info.resource_id或习题id
	 */
	private String resourceId;

	/**
	 * 资源类型: 1视频 2课件 3作业
	 */
	private Integer resourceType;

	/**
	 * 是否主资源: 1是 0否
	 */
	private Integer isPrimary;

	/**
	 * 排序值
	 */
	private Integer sortOrder;


	public void setId(Integer id){
		this.id = id;
	}

	public Integer getId(){
		return this.id;
	}

	public void setLessonId(String lessonId){
		this.lessonId = lessonId;
	}

	public String getLessonId(){
		return this.lessonId;
	}

	public void setResourceId(String resourceId){
		this.resourceId = resourceId;
	}

	public String getResourceId(){
		return this.resourceId;
	}

	public void setResourceType(Integer resourceType){
		this.resourceType = resourceType;
	}

	public Integer getResourceType(){
		return this.resourceType;
	}

	public void setIsPrimary(Integer isPrimary){
		this.isPrimary = isPrimary;
	}

	public Integer getIsPrimary(){
		return this.isPrimary;
	}

	public void setSortOrder(Integer sortOrder){
		this.sortOrder = sortOrder;
	}

	public Integer getSortOrder(){
		return this.sortOrder;
	}

	@Override
	public String toString (){
		return "主键ID:"+(id == null ? "空" : id)+"，课时ID:"+(lessonId == null ? "空" : lessonId)+"，资源ID，对应resource_info.resource_id或习题id:"+(resourceId == null ? "空" : resourceId)+"，资源类型: 1视频 2课件 3作业:"+(resourceType == null ? "空" : resourceType)+"，是否主资源: 1是 0否:"+(isPrimary == null ? "空" : isPrimary)+"，排序值:"+(sortOrder == null ? "空" : sortOrder);
	}
}
