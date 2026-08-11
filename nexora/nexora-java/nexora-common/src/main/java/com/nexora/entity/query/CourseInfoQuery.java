package com.nexora.entity.query;

import java.util.Date;


/**
 * 课程（教材）表参数
 */
public class CourseInfoQuery extends BaseParam {


	/**
	 * 课程ID
	 */
	private String courseId;

	private String courseIdFuzzy;

	/**
	 * 课程名
	 */
	private String courseName;

	private String courseNameFuzzy;

	/**
	 * 封面URL
	 */
	private String cover;

	private String coverFuzzy;

	/**
	 * 学段【冗余：学生端按学段过滤主筛选键】
	 */
	private String stage;

	private String stageFuzzy;

	/**
	 * 学科
	 */
	private String subject;

	private String subjectFuzzy;

	/**
	 * 难度：1-3星
	 */
	private Integer difficulty;

	/**
	 * 简介
	 */
	private String description;

	private String descriptionFuzzy;

	/**
	 * 详细介绍
	 */
	private String intro;

	private String introFuzzy;

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
	private String createTime;

	private String createTimeStart;

	private String createTimeEnd;

	/**
	 * 更新时间
	 */
	private String updateTime;

	private String updateTimeStart;

	private String updateTimeEnd;


	public void setCourseId(String courseId){
		this.courseId = courseId;
	}

	public String getCourseId(){
		return this.courseId;
	}

	public void setCourseIdFuzzy(String courseIdFuzzy){
		this.courseIdFuzzy = courseIdFuzzy;
	}

	public String getCourseIdFuzzy(){
		return this.courseIdFuzzy;
	}

	public void setCourseName(String courseName){
		this.courseName = courseName;
	}

	public String getCourseName(){
		return this.courseName;
	}

	public void setCourseNameFuzzy(String courseNameFuzzy){
		this.courseNameFuzzy = courseNameFuzzy;
	}

	public String getCourseNameFuzzy(){
		return this.courseNameFuzzy;
	}

	public void setCover(String cover){
		this.cover = cover;
	}

	public String getCover(){
		return this.cover;
	}

	public void setCoverFuzzy(String coverFuzzy){
		this.coverFuzzy = coverFuzzy;
	}

	public String getCoverFuzzy(){
		return this.coverFuzzy;
	}

	public void setStage(String stage){
		this.stage = stage;
	}

	public String getStage(){
		return this.stage;
	}

	public void setStageFuzzy(String stageFuzzy){
		this.stageFuzzy = stageFuzzy;
	}

	public String getStageFuzzy(){
		return this.stageFuzzy;
	}

	public void setSubject(String subject){
		this.subject = subject;
	}

	public String getSubject(){
		return this.subject;
	}

	public void setSubjectFuzzy(String subjectFuzzy){
		this.subjectFuzzy = subjectFuzzy;
	}

	public String getSubjectFuzzy(){
		return this.subjectFuzzy;
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

	public void setDescriptionFuzzy(String descriptionFuzzy){
		this.descriptionFuzzy = descriptionFuzzy;
	}

	public String getDescriptionFuzzy(){
		return this.descriptionFuzzy;
	}

	public void setIntro(String intro){
		this.intro = intro;
	}

	public String getIntro(){
		return this.intro;
	}

	public void setIntroFuzzy(String introFuzzy){
		this.introFuzzy = introFuzzy;
	}

	public String getIntroFuzzy(){
		return this.introFuzzy;
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

	public void setCreateTime(String createTime){
		this.createTime = createTime;
	}

	public String getCreateTime(){
		return this.createTime;
	}

	public void setCreateTimeStart(String createTimeStart){
		this.createTimeStart = createTimeStart;
	}

	public String getCreateTimeStart(){
		return this.createTimeStart;
	}
	public void setCreateTimeEnd(String createTimeEnd){
		this.createTimeEnd = createTimeEnd;
	}

	public String getCreateTimeEnd(){
		return this.createTimeEnd;
	}

	public void setUpdateTime(String updateTime){
		this.updateTime = updateTime;
	}

	public String getUpdateTime(){
		return this.updateTime;
	}

	public void setUpdateTimeStart(String updateTimeStart){
		this.updateTimeStart = updateTimeStart;
	}

	public String getUpdateTimeStart(){
		return this.updateTimeStart;
	}
	public void setUpdateTimeEnd(String updateTimeEnd){
		this.updateTimeEnd = updateTimeEnd;
	}

	public String getUpdateTimeEnd(){
		return this.updateTimeEnd;
	}

}
