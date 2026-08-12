package com.nexora.entity.po;

import java.util.Date;
import com.nexora.entity.enums.DateTimePatternEnum;
import com.nexora.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 课时学习进度表
 */
public class CourseStudyLessonProgress implements Serializable {


	/**
	 * 主键
	 */
	private Integer id;

	/**
	 * 学生
	 */
	private Integer userId;

	/**
	 * 课程【冗余】
	 */
	private String courseId;

	/**
	 * 课时
	 */
	private String lessonId;

	/**
	 * 视频最后播放位置（秒），续播锚点
	 */
	private Integer playPosition;

	/**
	 * 学习时长（秒）
	 */
	private Integer studyDuration;

	/**
	 * 0未完成 1已完成
	 */
	private Integer finished;

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

	public void setLessonId(String lessonId){
		this.lessonId = lessonId;
	}

	public String getLessonId(){
		return this.lessonId;
	}

	public void setPlayPosition(Integer playPosition){
		this.playPosition = playPosition;
	}

	public Integer getPlayPosition(){
		return this.playPosition;
	}

	public void setStudyDuration(Integer studyDuration){
		this.studyDuration = studyDuration;
	}

	public Integer getStudyDuration(){
		return this.studyDuration;
	}

	public void setFinished(Integer finished){
		this.finished = finished;
	}

	public Integer getFinished(){
		return this.finished;
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
		return "主键:"+(id == null ? "空" : id)+"，学生:"+(userId == null ? "空" : userId)+"，课程【冗余】:"+(courseId == null ? "空" : courseId)+"，课时:"+(lessonId == null ? "空" : lessonId)+"，视频最后播放位置（秒），续播锚点:"+(playPosition == null ? "空" : playPosition)+"，学习时长（秒）:"+(studyDuration == null ? "空" : studyDuration)+"，0未完成 1已完成:"+(finished == null ? "空" : finished)+"，完成时间:"+(finishTime == null ? "空" : DateUtil.format(finishTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，更新时间:"+(updateTime == null ? "空" : DateUtil.format(updateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
