package com.nexora.entity.query;

import java.util.Date;

/**
 * 课时通关测验配置参数
 */
public class CourseLessonQuizQuery extends BaseParam {

	/**
	 * 课时ID
	 */
	private String lessonId;

	private String lessonIdFuzzy;

	/**
	 * 课程ID
	 */
	private String courseId;

	private String courseIdFuzzy;

	/**
	 * 测验方式
	 */
	private Integer quizMode;

	/**
	 * 严格门禁
	 */
	private Integer unlockNext;

	/**
	 * 状态
	 */
	private Integer status;

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

	public void setLessonId(String lessonId) {
		this.lessonId = lessonId;
	}

	public String getLessonId() {
		return this.lessonId;
	}

	public void setLessonIdFuzzy(String lessonIdFuzzy) {
		this.lessonIdFuzzy = lessonIdFuzzy;
	}

	public String getLessonIdFuzzy() {
		return this.lessonIdFuzzy;
	}

	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}

	public String getCourseId() {
		return this.courseId;
	}

	public void setCourseIdFuzzy(String courseIdFuzzy) {
		this.courseIdFuzzy = courseIdFuzzy;
	}

	public String getCourseIdFuzzy() {
		return this.courseIdFuzzy;
	}

	public void setQuizMode(Integer quizMode) {
		this.quizMode = quizMode;
	}

	public Integer getQuizMode() {
		return this.quizMode;
	}

	public void setUnlockNext(Integer unlockNext) {
		this.unlockNext = unlockNext;
	}

	public Integer getUnlockNext() {
		return this.unlockNext;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getStatus() {
		return this.status;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getCreateTime() {
		return this.createTime;
	}

	public void setCreateTimeStart(String createTimeStart) {
		this.createTimeStart = createTimeStart;
	}

	public String getCreateTimeStart() {
		return this.createTimeStart;
	}

	public void setCreateTimeEnd(String createTimeEnd) {
		this.createTimeEnd = createTimeEnd;
	}

	public String getCreateTimeEnd() {
		return this.createTimeEnd;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public String getUpdateTime() {
		return this.updateTime;
	}

	public void setUpdateTimeStart(String updateTimeStart) {
		this.updateTimeStart = updateTimeStart;
	}

	public String getUpdateTimeStart() {
		return this.updateTimeStart;
	}

	public void setUpdateTimeEnd(String updateTimeEnd) {
		this.updateTimeEnd = updateTimeEnd;
	}

	public String getUpdateTimeEnd() {
		return this.updateTimeEnd;
	}
}