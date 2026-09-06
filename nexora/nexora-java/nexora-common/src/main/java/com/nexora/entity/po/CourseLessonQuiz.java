package com.nexora.entity.po;

import java.io.Serializable;
import java.util.Date;
import com.nexora.entity.enums.DateTimePatternEnum;
import com.nexora.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 课时通关测验配置表
 */
public class CourseLessonQuiz implements Serializable {

	/**
	 * 课时ID（1:1 关联 course_chapter_lesson.lesson_id）
	 */
	private String lessonId;

	/**
	 * 课程ID【冗余：免 join 查询】
	 */
	private String courseId;

	/**
	 * 0关闭 1题库选题 2AI生成
	 */
	private Integer quizMode;

	/**
	 * 题库模式已选题ID列表（JSON 数组）
	 */
	private String questionIds;

	/**
	 * AI模式题量
	 */
	private Integer questionCount;

	/**
	 * AI模式难度 1-3
	 */
	private Integer difficulty;

	/**
	 * 及格分（百分制）
	 */
	private Integer passScore;

	/**
	 * 0宽松考核(不进顺序) 1严格门禁(通过才解锁下一课时)
	 */
	private Integer unlockNext;

	/**
	 * 弹性扩展配置（JSON：限时/重考次数/乱序等）
	 */
	private String quizConfig;

	/**
	 * 1启用 0停用
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

	public String getLessonId() {
		return lessonId;
	}

	public void setLessonId(String lessonId) {
		this.lessonId = lessonId;
	}

	public String getCourseId() {
		return courseId;
	}

	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}

	public Integer getQuizMode() {
		return quizMode;
	}

	public void setQuizMode(Integer quizMode) {
		this.quizMode = quizMode;
	}

	public String getQuestionIds() {
		return questionIds;
	}

	public void setQuestionIds(String questionIds) {
		this.questionIds = questionIds;
	}

	public Integer getQuestionCount() {
		return questionCount;
	}

	public void setQuestionCount(Integer questionCount) {
		this.questionCount = questionCount;
	}

	public Integer getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(Integer difficulty) {
		this.difficulty = difficulty;
	}

	public Integer getPassScore() {
		return passScore;
	}

	public void setPassScore(Integer passScore) {
		this.passScore = passScore;
	}

	public Integer getUnlockNext() {
		return unlockNext;
	}

	public void setUnlockNext(Integer unlockNext) {
		this.unlockNext = unlockNext;
	}

	public String getQuizConfig() {
		return quizConfig;
	}

	public void setQuizConfig(String quizConfig) {
		this.quizConfig = quizConfig;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	@Override
	public String toString() {
		return "课时ID:" + (lessonId == null ? "空" : lessonId)
				+ "，课程ID:" + (courseId == null ? "空" : courseId)
				+ "，测验方式:" + (quizMode == null ? "空" : quizMode)
				+ "，已选题:" + (questionIds == null ? "空" : questionIds)
				+ "，AI题量:" + (questionCount == null ? "空" : questionCount)
				+ "，AI难度:" + (difficulty == null ? "空" : difficulty)
				+ "，及格分:" + (passScore == null ? "空" : passScore)
				+ "，严格门禁:" + (unlockNext == null ? "空" : unlockNext)
				+ "，扩展配置:" + (quizConfig == null ? "空" : quizConfig)
				+ "，状态:" + (status == null ? "空" : status)
				+ "，创建时间:" + (createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))
				+ "，更新时间:" + (updateTime == null ? "空" : DateUtil.format(updateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}