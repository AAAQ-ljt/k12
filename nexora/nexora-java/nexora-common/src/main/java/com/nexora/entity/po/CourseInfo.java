package com.nexora.entity.po;

import java.util.Date;
import com.nexora.entity.enums.DateTimePatternEnum;
import com.nexora.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 课程（教材）表
 */
public class CourseInfo implements Serializable {


	/**
	 * 课程ID
	 */
	private String courseId;

	/**
	 * 课程名
	 */
	private String courseName;

	/**
	 * 封面URL
	 */
	private String cover;

	/**
	 * 学段【冗余：学生端按学段过滤主筛选键】
	 */
	private String stage;

	/**
	 * 年级【学生端按年级过滤主筛选键】
	 */
	private String grade;

	/**
	 * 学科
	 */
	private String subject;

	/**
	 * 难度：1-3星
	 */
	private Integer difficulty;

	/**
	 * 简介
	 */
	private String description;

	/**
	 * 详细介绍
	 */
	private String intro;

	/**
	 * 课时总数【冗余：课时增删时同事务维护】
	 */
	private Integer lessonCount;

	/**
	 * 学习人数【冗余：学习行为触发计数】
	 */
	private Integer studyCount;

	/**
	 * 排序
	 */
	private Integer sort;

	/**
	 * 状态：1上架 0下架
	 */
	private Integer status;

	/**
	 * 创建人（管理员）
	 */
	private Integer createBy;

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

	public void setCover(String cover){
		this.cover = cover;
	}

	public String getCover(){
		return this.cover;
	}

	public void setStage(String stage){
		this.stage = stage;
	}

	public String getStage(){
		return this.stage;
	}

	public void setGrade(String grade){
		this.grade = grade;
	}

	public String getGrade(){
		return this.grade;
	}

	public void setSubject(String subject){
		this.subject = subject;
	}

	public String getSubject(){
		return this.subject;
	}

	public void setDifficulty(Integer difficulty){
		this.difficulty = difficulty;
	}

	public Integer getDifficulty(){
		return this.difficulty;
	}

	public void setDescription(String description){
		this.description = description;
	}

	public String getDescription(){
		return this.description;
	}

	public void setIntro(String intro){
		this.intro = intro;
	}

	public String getIntro(){
		return this.intro;
	}

	public void setLessonCount(Integer lessonCount){
		this.lessonCount = lessonCount;
	}

	public Integer getLessonCount(){
		return this.lessonCount;
	}

	public void setStudyCount(Integer studyCount){
		this.studyCount = studyCount;
	}

	public Integer getStudyCount(){
		return this.studyCount;
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

	public void setCreateBy(Integer createBy){
		this.createBy = createBy;
	}

	public Integer getCreateBy(){
		return this.createBy;
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
		return "课程ID:"+(courseId == null ? "空" : courseId)+"，课程名:"+(courseName == null ? "空" : courseName)+"，封面URL:"+(cover == null ? "空" : cover)+"，学段【冗余：学生端按学段过滤主筛选键】:"+(stage == null ? "空" : stage)+"，年级【学生端按年级过滤主筛选键】:"+(grade == null ? "空" : grade)+"，学科:"+(subject == null ? "空" : subject)+"，难度：1-3星:"+(difficulty == null ? "空" : difficulty)+"，简介:"+(description == null ? "空" : description)+"，详细介绍:"+(intro == null ? "空" : intro)+"，课时总数【冗余：课时增删时同事务维护】:"+(lessonCount == null ? "空" : lessonCount)+"，学习人数【冗余：学习行为触发计数】:"+(studyCount == null ? "空" : studyCount)+"，排序:"+(sort == null ? "空" : sort)+"，状态：1上架 0下架:"+(status == null ? "空" : status)+"，创建人（管理员）:"+(createBy == null ? "空" : createBy)+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，更新时间:"+(updateTime == null ? "空" : DateUtil.format(updateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
