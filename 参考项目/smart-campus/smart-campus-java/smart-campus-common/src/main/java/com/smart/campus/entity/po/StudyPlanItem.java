package com.smart.campus.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import com.smart.campus.entity.enums.DateTimePatternEnum;
import com.smart.campus.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 学生学习计划明细表
 */
public class StudyPlanItem implements Serializable {


	/**
	 * 计划明细ID
	 */
	private Long itemId;

	/**
	 * 学习计划ID
	 */
	private String planId;

	/**
	 * 课程ID
	 */
	private String courseId;

	/**
	 * 章节ID
	 */
	private String chapterId;

	/**
	 * 课时ID
	 */
	private String lessonId;

	/**
	 * 开始时间
	 */
	private Integer startTime;

	/**
	 * 完成日期
	 */
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date complateTime;

	/**
	 * 0未开始 1进行中 2完成
	 */
	private Integer status;

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;


	public void setItemId(Long itemId){
		this.itemId = itemId;
	}

	public Long getItemId(){
		return this.itemId;
	}

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

	public void setChapterId(String chapterId){
		this.chapterId = chapterId;
	}

	public String getChapterId(){
		return this.chapterId;
	}

	public void setLessonId(String lessonId){
		this.lessonId = lessonId;
	}

	public String getLessonId(){
		return this.lessonId;
	}

	public void setStartTime(Integer startTime){
		this.startTime = startTime;
	}

	public Integer getStartTime(){
		return this.startTime;
	}

	public void setComplateTime(Date complateTime){
		this.complateTime = complateTime;
	}

	public Date getComplateTime(){
		return this.complateTime;
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

	@Override
	public String toString (){
		return "计划明细ID:"+(itemId == null ? "空" : itemId)+"，学习计划ID:"+(planId == null ? "空" : planId)+"，课程ID:"+(courseId == null ? "空" : courseId)+"，章节ID:"+(chapterId == null ? "空" : chapterId)+"，课时ID:"+(lessonId == null ? "空" : lessonId)+"，开始时间:"+(startTime == null ? "空" : startTime)+"，完成日期:"+(complateTime == null ? "空" : DateUtil.format(complateTime, DateTimePatternEnum.YYYY_MM_DD.getPattern()))+"，0未开始 1进行中 2完成:"+(status == null ? "空" : status)+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
