package com.nexora.entity.po;

import java.util.Date;
import com.nexora.entity.enums.DateTimePatternEnum;
import com.nexora.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 学习日志表
 */
public class CourseStudyLog implements Serializable {


	/**
	 * 主键
	 */
	private Long id;

	/**
	 * 学生
	 */
	private Integer userId;

	/**
	 * 课程
	 */
	private String courseId;

	/**
	 * 课时
	 */
	private String lessonId;

	/**
	 * 学习日期【冗余：连续打卡/时长统计按天聚合】
	 */
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date studyDate;

	/**
	 * 本次时长（秒）
	 */
	private Integer duration;

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;


	public void setId(Long id){
		this.id = id;
	}

	public Long getId(){
		return this.id;
	}

	public void setUserId(Integer userId){
		this.userId = userId;
	}

	public Integer getUserId(){
		return this.userId;
	}

	public void setCourseId(String courseId){
		this.courseId = courseId;
	}

	public String getCourseId(){
		return this.courseId;
	}

	public void setLessonId(String lessonId){
		this.lessonId = lessonId;
	}

	public String getLessonId(){
		return this.lessonId;
	}

	public void setStudyDate(Date studyDate){
		this.studyDate = studyDate;
	}

	public Date getStudyDate(){
		return this.studyDate;
	}

	public void setDuration(Integer duration){
		this.duration = duration;
	}

	public Integer getDuration(){
		return this.duration;
	}

	public void setCreateTime(Date createTime){
		this.createTime = createTime;
	}

	public Date getCreateTime(){
		return this.createTime;
	}

	@Override
	public String toString (){
		return "主键:"+(id == null ? "空" : id)+"，学生:"+(userId == null ? "空" : userId)+"，课程:"+(courseId == null ? "空" : courseId)+"，课时:"+(lessonId == null ? "空" : lessonId)+"，学习日期【冗余：连续打卡/时长统计按天聚合】:"+(studyDate == null ? "空" : DateUtil.format(studyDate, DateTimePatternEnum.YYYY_MM_DD.getPattern()))+"，本次时长（秒）:"+(duration == null ? "空" : duration)+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
