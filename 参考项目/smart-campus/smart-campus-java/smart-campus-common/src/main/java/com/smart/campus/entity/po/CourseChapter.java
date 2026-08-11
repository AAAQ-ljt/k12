package com.smart.campus.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;


/**
 * 课程章节表
 */
public class CourseChapter implements Serializable {


	/**
	 * 主键ID
	 */
	private String chapterId;

	/**
	 * 所属课程ID
	 */
	private String courseId;

	/**
	 * 章节名称
	 */
	private String chapterName;

	/**
	 * 章节说明
	 */
	private String description;

	/**
	 * 排序值
	 */
	private Integer sortOrder;


	public void setChapterId(String chapterId){
		this.chapterId = chapterId;
	}

	public String getChapterId(){
		return this.chapterId;
	}

	public void setCourseId(String courseId){
		this.courseId = courseId;
	}

	public String getCourseId(){
		return this.courseId;
	}

	public void setChapterName(String chapterName){
		this.chapterName = chapterName;
	}

	public String getChapterName(){
		return this.chapterName;
	}

	public void setDescription(String description){
		this.description = description;
	}

	public String getDescription(){
		return this.description;
	}

	public void setSortOrder(Integer sortOrder){
		this.sortOrder = sortOrder;
	}

	public Integer getSortOrder(){
		return this.sortOrder;
	}

	@Override
	public String toString (){
		return "主键ID:"+(chapterId == null ? "空" : chapterId)+"，所属课程ID:"+(courseId == null ? "空" : courseId)+"，章节名称:"+(chapterName == null ? "空" : chapterName)+"，章节说明:"+(description == null ? "空" : description)+"，排序值:"+(sortOrder == null ? "空" : sortOrder);
	}
}
