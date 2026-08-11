package com.nexora.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import com.nexora.entity.enums.DateTimePatternEnum;
import com.nexora.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 课时表
 */
public class CourseChapterLesson implements Serializable {


	/**
	 * 课时ID
	 */
	private String lessonId;

	/**
	 * 所属章节
	 */
	private String chapterId;

	/**
	 * 所属课程【冗余：免join章节直查课程课时树】
	 */
	private String courseId;

	/**
	 * 课时名
	 */
	private String lessonName;

	/**
	 * 课时摘要
	 */
	private String summary;

	/**
	 * 视频时长（秒）【冗余：自主视频资源同步，续播UI免查资源表】
	 */
	private Integer videoDuration;

	/**
	 * 排序
	 */
	private Integer sort;

	/**
	 * 状态：0正常 1停用
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


	public void setLessonId(String lessonId){
		this.lessonId = lessonId;
	}

	public String getLessonId(){
		return this.lessonId;
	}

	public void setChapterId(String chapterId){
		this.chapterId = chapterId;
	}

	public String getChapterId(){
		return this.chapterId;
	}

	public void setCourseId(String courseId){
		this.courseId = courseId;
	}

	public String getCourseId(){
		return this.courseId;
	}

	public void setLessonName(String lessonName){
		this.lessonName = lessonName;
	}

	public String getLessonName(){
		return this.lessonName;
	}

	public void setSummary(String summary){
		this.summary = summary;
	}

	public String getSummary(){
		return this.summary;
	}

	public void setVideoDuration(Integer videoDuration){
		this.videoDuration = videoDuration;
	}

	public Integer getVideoDuration(){
		return this.videoDuration;
	}

	public void setSort(Integer sort){
		this.sort = sort;
	}

	public Integer getSort(){
		return this.sort;
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
		return "课时ID:"+(lessonId == null ? "空" : lessonId)+"，所属章节:"+(chapterId == null ? "空" : chapterId)+"，所属课程【冗余：免join章节直查课程课时树】:"+(courseId == null ? "空" : courseId)+"，课时名:"+(lessonName == null ? "空" : lessonName)+"，课时摘要:"+(summary == null ? "空" : summary)+"，视频时长（秒）【冗余：自主视频资源同步，续播UI免查资源表】:"+(videoDuration == null ? "空" : videoDuration)+"，排序:"+(sort == null ? "空" : sort)+"，状态：0正常 1停用:"+(status == null ? "空" : status)+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，更新时间:"+(updateTime == null ? "空" : DateUtil.format(updateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
