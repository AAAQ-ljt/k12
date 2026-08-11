package com.smart.campus.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;


/**
 * 课程收藏
 */
public class CourseUserCollection implements Serializable {


	/**
	 * 主键ID
	 */
	private String courseId;

	/**
	 * 主键ID
	 */
	private Integer userId;


	public void setCourseId(String courseId){
		this.courseId = courseId;
	}

	public String getCourseId(){
		return this.courseId;
	}

	public void setUserId(Integer userId){
		this.userId = userId;
	}

	public Integer getUserId(){
		return this.userId;
	}

	@Override
	public String toString (){
		return "主键ID:"+(courseId == null ? "空" : courseId)+"，主键ID:"+(userId == null ? "空" : userId);
	}
}
