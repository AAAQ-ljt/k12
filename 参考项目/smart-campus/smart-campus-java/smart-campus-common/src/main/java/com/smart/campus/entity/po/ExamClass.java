package com.smart.campus.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;


/**
 * 考试班级关联表
 */
public class ExamClass implements Serializable {


	/**
	 * 考试ID
	 */
	private String examId;

	/**
	 * 班级ID
	 */
	private Integer classId;


	public void setExamId(String examId){
		this.examId = examId;
	}

	public String getExamId(){
		return this.examId;
	}

	public void setClassId(Integer classId){
		this.classId = classId;
	}

	public Integer getClassId(){
		return this.classId;
	}

	@Override
	public String toString (){
		return "考试ID:"+(examId == null ? "空" : examId)+"，班级ID:"+(classId == null ? "空" : classId);
	}
}
