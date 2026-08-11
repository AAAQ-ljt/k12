package com.smart.campus.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import com.smart.campus.entity.enums.DateTimePatternEnum;
import com.smart.campus.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 学生课时学习进度表
 */
public class CourseStudyLessonProgress implements Serializable {


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
	 * 章节ID，对应 course_chapter.chapter_id
	 */
	private String chapterId;

	/**
	 * 课时ID，对应 course_chapter_lesson.lesson_id
	 */
	private String lessonId;

	/**
	 * 视频资源ID，对应 resource_info.resource_id
	 */
	private Integer videoResourceId;

	/**
	 * 累计学习时长，单位秒，重复观看可累计
	 */
	private Integer studySeconds;

	/**
	 * 上次播放位置，单位秒
	 */
	private Integer lastPositionSeconds;

	/**
	 * 历史最远播放位置，单位秒
	 */
	private Integer maxPositionSeconds;

	/**
	 * 视频总时长，单位秒
	 */
	private Integer videoDurationSeconds;

	/**
	 * 是否完成: 0否 1是
	 */
	private Integer isCompleted;

	/**
	 * 完成时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date completeTime;

	/**
	 * 最后学习时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lastStudyTime;

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

	public void setVideoResourceId(Integer videoResourceId){
		this.videoResourceId = videoResourceId;
	}

	public Integer getVideoResourceId(){
		return this.videoResourceId;
	}

	public void setStudySeconds(Integer studySeconds){
		this.studySeconds = studySeconds;
	}

	public Integer getStudySeconds(){
		return this.studySeconds;
	}

	public void setLastPositionSeconds(Integer lastPositionSeconds){
		this.lastPositionSeconds = lastPositionSeconds;
	}

	public Integer getLastPositionSeconds(){
		return this.lastPositionSeconds;
	}

	public void setMaxPositionSeconds(Integer maxPositionSeconds){
		this.maxPositionSeconds = maxPositionSeconds;
	}

	public Integer getMaxPositionSeconds(){
		return this.maxPositionSeconds;
	}

	public void setVideoDurationSeconds(Integer videoDurationSeconds){
		this.videoDurationSeconds = videoDurationSeconds;
	}

	public Integer getVideoDurationSeconds(){
		return this.videoDurationSeconds;
	}

	public void setIsCompleted(Integer isCompleted){
		this.isCompleted = isCompleted;
	}

	public Integer getIsCompleted(){
		return this.isCompleted;
	}

	public void setCompleteTime(Date completeTime){
		this.completeTime = completeTime;
	}

	public Date getCompleteTime(){
		return this.completeTime;
	}

	public void setLastStudyTime(Date lastStudyTime){
		this.lastStudyTime = lastStudyTime;
	}

	public Date getLastStudyTime(){
		return this.lastStudyTime;
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
		return "主键ID:"+(id == null ? "空" : id)+"，学生ID，对应 user_info.user_id:"+(userId == null ? "空" : userId)+"，课程ID，对应 course_info.course_id:"+(courseId == null ? "空" : courseId)+"，章节ID，对应 course_chapter.chapter_id:"+(chapterId == null ? "空" : chapterId)+"，课时ID，对应 course_chapter_lesson.lesson_id:"+(lessonId == null ? "空" : lessonId)+"，视频资源ID，对应 resource_info.resource_id:"+(videoResourceId == null ? "空" : videoResourceId)+"，累计学习时长，单位秒，重复观看可累计:"+(studySeconds == null ? "空" : studySeconds)+"，上次播放位置，单位秒:"+(lastPositionSeconds == null ? "空" : lastPositionSeconds)+"，历史最远播放位置，单位秒:"+(maxPositionSeconds == null ? "空" : maxPositionSeconds)+"，视频总时长，单位秒:"+(videoDurationSeconds == null ? "空" : videoDurationSeconds)+"，是否完成: 0否 1是:"+(isCompleted == null ? "空" : isCompleted)+"，完成时间:"+(completeTime == null ? "空" : DateUtil.format(completeTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，最后学习时间:"+(lastStudyTime == null ? "空" : DateUtil.format(lastStudyTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，更新时间:"+(updateTime == null ? "空" : DateUtil.format(updateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
