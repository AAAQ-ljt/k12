package com.nexora.entity.po;

import java.util.Date;
import com.nexora.entity.enums.DateTimePatternEnum;
import com.nexora.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 课程学习进度表
 */
public class CourseStudyProgress implements Serializable {


	/**
	 * 主键
	 */
	private Integer id;

	/**
	 * 学生
	 */
	private Integer userId;

	/**
	 * 课程
	 */
	private String courseId;

	/**
	 * 已学课时数【冗余：Redis缓冲聚合后异步回写】
	 */
	private Integer studiedLessons;

	/**
	 * 课时总数【冗余快照】
	 */
	private Integer totalLessons;

	/**
	 * 进度百分比【冗余：列表直读】
	 */
	private Integer progress;

	/**
	 * 累计学习时长（秒）
	 */
	private Integer studyDuration;

	/**
	 * 最近学习课时
	 */
	private String lastLessonId;

	/**
	 * 完成时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date finishTime;

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	/**
	 * 更新时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date updateTime;


	public void setId(Integer id){
		this.id = id;
	}

	public Integer getId(){
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

	public void setStudiedLessons(Integer studiedLessons){
		this.studiedLessons = studiedLessons;
	}

	public Integer getStudiedLessons(){
		return this.studiedLessons;
	}

	public void setTotalLessons(Integer totalLessons){
		this.totalLessons = totalLessons;
	}

	public Integer getTotalLessons(){
		return this.totalLessons;
	}

	public void setProgress(Integer progress){
		this.progress = progress;
	}

	public Integer getProgress(){
		return this.progress;
	}

	public void setStudyDuration(Integer studyDuration){
		this.studyDuration = studyDuration;
	}

	public Integer getStudyDuration(){
		return this.studyDuration;
	}

	public void setLastLessonId(String lastLessonId){
		this.lastLessonId = lastLessonId;
	}

	public String getLastLessonId(){
		return this.lastLessonId;
	}

	public void setFinishTime(Date finishTime){
		this.finishTime = finishTime;
	}

	public Date getFinishTime(){
		return this.finishTime;
	}

	public void setCreateTime(Date createTime){
		this.createTime = createTime;
	}

	public Date getCreateTime(){
		return this.createTime;
	}

	public void setUpdateTime(Date updateTime){
		this.updateTime = updateTime;
	}

	public Date getUpdateTime(){
		return this.updateTime;
	}

	@Override
	public String toString (){
		return "主键:"+(id == null ? "空" : id)+"，学生:"+(userId == null ? "空" : userId)+"，课程:"+(courseId == null ? "空" : courseId)+"，已学课时数【冗余：Redis缓冲聚合后异步回写】:"+(studiedLessons == null ? "空" : studiedLessons)+"，课时总数【冗余快照】:"+(totalLessons == null ? "空" : totalLessons)+"，进度百分比【冗余：列表直读】:"+(progress == null ? "空" : progress)+"，累计学习时长（秒）:"+(studyDuration == null ? "空" : studyDuration)+"，最近学习课时:"+(lastLessonId == null ? "空" : lastLessonId)+"，完成时间:"+(finishTime == null ? "空" : DateUtil.format(finishTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，更新时间:"+(updateTime == null ? "空" : DateUtil.format(updateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
