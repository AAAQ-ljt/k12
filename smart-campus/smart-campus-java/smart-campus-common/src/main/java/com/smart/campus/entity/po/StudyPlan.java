package com.smart.campus.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;


/**
 * 学生学习计划主表
 */
public class StudyPlan implements Serializable {


	/**
	 * 学习计划ID
	 */
	private String planId;

	/**
	 * 课程ID
	 */
	private String courseId;

	/**
	 * 学生ID
	 */
	private Integer studentId;

	/**
	 * 0未开始 1进行中 2完成
	 */
	private Integer status;

	/**
	 * 计划说明
	 */
	private String description;


	public void setPlanId(String planId){
		this.planId = planId;
	}

	public String getPlanId(){
		return this.planId;
	}

	public void setCourseId(String courseId){
		this.courseId = courseId;
	}

	public String getCourseId(){
		return this.courseId;
	}

	public void setStudentId(Integer studentId){
		this.studentId = studentId;
	}

	public Integer getStudentId(){
		return this.studentId;
	}

	public void setStatus(Integer status){
		this.status = status;
	}

	public Integer getStatus(){
		return this.status;
	}

	public void setDescription(String description){
		this.description = description;
	}

	public String getDescription(){
		return this.description;
	}

	@Override
	public String toString (){
		return "学习计划ID:"+(planId == null ? "空" : planId)+"，课程ID:"+(courseId == null ? "空" : courseId)+"，学生ID:"+(studentId == null ? "空" : studentId)+"，0未开始 1进行中 2完成:"+(status == null ? "空" : status)+"，计划说明:"+(description == null ? "空" : description);
	}
}
