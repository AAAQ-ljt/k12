package com.smart.campus.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import com.smart.campus.entity.enums.DateTimePatternEnum;
import com.smart.campus.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 学生课程学习进度表
 */
public class CourseStudyProgress implements Serializable {


	/**
	 * 主键ID
	 */
	private Long id;

	/**
	 * 学生ID，对应 user_info.user_id
	 */
	private Integer userId;

	/**
	 * 课程ID，对应 course_info.course_id
	 */
	private String courseId;

	/**
	 * 当前学习章节ID，对应 course_chapter.chapter_id
	 */
	private String currentChapterId;

	/**
	 * 当前学习课时ID，对应 course_chapter_lesson.lesson_id
	 */
	private String currentLessonId;

	/**
	 * 累计学习时长，单位秒，重复观看可累计
	 */
	private Integer studySeconds;

	/**
	 * 最后学习时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lastStudyTime;

	/**
	 * 学习状态: 0未开始 1学习中 2已完成
	 */
	private Integer status;

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

	public void setCurrentChapterId(String currentChapterId){
		this.currentChapterId = currentChapterId;
	}

	public String getCurrentChapterId(){
		return this.currentChapterId;
	}

	public void setCurrentLessonId(String currentLessonId){
		this.currentLessonId = currentLessonId;
	}

	public String getCurrentLessonId(){
		return this.currentLessonId;
	}

	public void setStudySeconds(Integer studySeconds){
		this.studySeconds = studySeconds;
	}

	public Integer getStudySeconds(){
		return this.studySeconds;
	}

	public void setLastStudyTime(Date lastStudyTime){
		this.lastStudyTime = lastStudyTime;
	}

	public Date getLastStudyTime(){
		return this.lastStudyTime;
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

	public void setUpdateTime(Date updateTime){
		this.updateTime = updateTime;
	}

	public Date getUpdateTime(){
		return this.updateTime;
	}

	@Override
	public String toString (){
		return "主键ID:"+(id == null ? "空" : id)+"，学生ID，对应 user_info.user_id:"+(userId == null ? "空" : userId)+"，课程ID，对应 course_info.course_id:"+(courseId == null ? "空" : courseId)+"，当前学习章节ID，对应 course_chapter.chapter_id:"+(currentChapterId == null ? "空" : currentChapterId)+"，当前学习课时ID，对应 course_chapter_lesson.lesson_id:"+(currentLessonId == null ? "空" : currentLessonId)+"，累计学习时长，单位秒，重复观看可累计:"+(studySeconds == null ? "空" : studySeconds)+"，最后学习时间:"+(lastStudyTime == null ? "空" : DateUtil.format(lastStudyTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，学习状态: 0未开始 1学习中 2已完成:"+(status == null ? "空" : status)+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，更新时间:"+(updateTime == null ? "空" : DateUtil.format(updateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
