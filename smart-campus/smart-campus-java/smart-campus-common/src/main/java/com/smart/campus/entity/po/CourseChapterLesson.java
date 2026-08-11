package com.smart.campus.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;


/**
 * 课程课时表
 */
public class CourseChapterLesson implements Serializable {


	/**
	 * 主键ID
	 */
	private String lessonId;

	/**
	 * 所属课程ID
	 */
	private String courseId;

	/**
	 * 所属章节ID
	 */
	private String chapterId;

	/**
	 * 课时名称
	 */
	private String lessonName;

	/**
	 * 排序值
	 */
	private Integer sortOrder;


	public void setLessonId(String lessonId){
		this.lessonId = lessonId;
	}

	public String getLessonId(){
		return this.lessonId;
	}

	public void setCourseId(String courseId){
		this.courseId = courseId;
	}

	public String getCourseId(){
		return this.courseId;
	}

	public void setChapterId(String chapterId){
		this.chapterId = chapterId;
	}

	public String getChapterId(){
		return this.chapterId;
	}

	public void setLessonName(String lessonName){
		this.lessonName = lessonName;
	}

	public String getLessonName(){
		return this.lessonName;
	}

	public void setSortOrder(Integer sortOrder){
		this.sortOrder = sortOrder;
	}

	public Integer getSortOrder(){
		return this.sortOrder;
	}

	@Override
	public String toString (){
		return "主键ID:"+(lessonId == null ? "空" : lessonId)+"，所属课程ID:"+(courseId == null ? "空" : courseId)+"，所属章节ID:"+(chapterId == null ? "空" : chapterId)+"，课时名称:"+(lessonName == null ? "空" : lessonName)+"，排序值:"+(sortOrder == null ? "空" : sortOrder);
	}
}
