package com.smart.campus.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;


/**
 * 课程班级关联表
 */
public class CourseClass implements Serializable {


	/**
	 * 课程ID
	 */
	private String courseId;

	/**
	 * 班级ID
	 */
	private Integer classId;


	public void setCourseId(String courseId){
		this.courseId = courseId;
	}

	public String getCourseId(){
		return this.courseId;
	}

	public void setClassId(Integer classId){
		this.classId = classId;
	}

	public Integer getClassId(){
		return this.classId;
	}

	@Override
	public String toString (){
		return "课程ID:"+(courseId == null ? "空" : courseId)+"，班级ID:"+(classId == null ? "空" : classId);
	}
}
