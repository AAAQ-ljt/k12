package com.smart.campus.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import com.smart.campus.entity.enums.DateTimePatternEnum;
import com.smart.campus.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 课程表
 */
public class CourseInfo implements Serializable {


	/**
	 * 主键ID
	 */
	private String courseId;

	/**
	 * 课程名称
	 */
	private String courseName;

	/**
	 * 课程封面资源ID，可为空，对应resource_info.resource_id
	 */
	private Integer coverResourceId;

	/**
	 * 授课老师ID，对应user_info.user_id
	 */
	private Integer teacherId;

	/**
	 * 课程简介
	 */
	private String description;

	/**
	 * 录制状态: 0录制中 1录制完成
	 */
	private Integer recordStatus;

	/**
	 * 课程状态: 1正常 0停用
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


	public void setCourseId(String courseId){
		this.courseId = courseId;
	}

	public String getCourseId(){
		return this.courseId;
	}

	public void setCourseName(String courseName){
		this.courseName = courseName;
	}

	public String getCourseName(){
		return this.courseName;
	}

	public void setCoverResourceId(Integer coverResourceId){
		this.coverResourceId = coverResourceId;
	}

	public Integer getCoverResourceId(){
		return this.coverResourceId;
	}

	public void setTeacherId(Integer teacherId){
		this.teacherId = teacherId;
	}

	public Integer getTeacherId(){
		return this.teacherId;
	}

	public void setDescription(String description){
		this.description = description;
	}

	public String getDescription(){
		return this.description;
	}

	public void setRecordStatus(Integer recordStatus){
		this.recordStatus = recordStatus;
	}

	public Integer getRecordStatus(){
		return this.recordStatus;
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
		return "主键ID:"+(courseId == null ? "空" : courseId)+"，课程名称:"+(courseName == null ? "空" : courseName)+"，课程封面资源ID，可为空，对应resource_info.resource_id:"+(coverResourceId == null ? "空" : coverResourceId)+"，授课老师ID，对应user_info.user_id:"+(teacherId == null ? "空" : teacherId)+"，课程简介:"+(description == null ? "空" : description)+"，录制状态: 0录制中 1录制完成:"+(recordStatus == null ? "空" : recordStatus)+"，课程状态: 1正常 0停用:"+(status == null ? "空" : status)+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，更新时间:"+(updateTime == null ? "空" : DateUtil.format(updateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
