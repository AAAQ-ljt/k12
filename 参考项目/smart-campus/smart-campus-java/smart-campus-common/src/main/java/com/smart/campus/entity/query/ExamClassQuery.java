package com.smart.campus.entity.query;



/**
 * 考试班级关联表参数
 */
public class ExamClassQuery extends BaseParam {


	/**
	 * 考试ID
	 */
	private String examId;

	private String examIdFuzzy;

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

	public void setExamIdFuzzy(String examIdFuzzy){
		this.examIdFuzzy = examIdFuzzy;
	}

	public String getExamIdFuzzy(){
		return this.examIdFuzzy;
	}

	public void setClassId(Integer classId){
		this.classId = classId;
	}

	public Integer getClassId(){
		return this.classId;
	}

}
