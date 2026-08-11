package com.smart.campus.entity.query;

import java.util.Date;


/**
 * 学生课时学习进度表参数
 */
public class CourseStudyLessonProgressQuery extends BaseParam {


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

	private String courseIdFuzzy;

	/**
	 * 章节ID，对应 course_chapter.chapter_id
	 */
	private String chapterId;

	private String chapterIdFuzzy;

	/**
	 * 课时ID，对应 course_chapter_lesson.lesson_id
	 */
	private String lessonId;

	private String lessonIdFuzzy;

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
	private String completeTime;

	private String completeTimeStart;

	private String completeTimeEnd;

	/**
	 * 最后学习时间
	 */
	private String lastStudyTime;

	private String lastStudyTimeStart;

	private String lastStudyTimeEnd;

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

	public void setCourseIdFuzzy(String courseIdFuzzy){
		this.courseIdFuzzy = courseIdFuzzy;
	}

	public String getCourseIdFuzzy(){
		return this.courseIdFuzzy;
	}

	public void setChapterId(String chapterId){
		this.chapterId = chapterId;
	}

	public String getChapterId(){
		return this.chapterId;
	}

	public void setChapterIdFuzzy(String chapterIdFuzzy){
		this.chapterIdFuzzy = chapterIdFuzzy;
	}

	public String getChapterIdFuzzy(){
		return this.chapterIdFuzzy;
	}

	public void setLessonId(String lessonId){
		this.lessonId = lessonId;
	}

	public String getLessonId(){
		return this.lessonId;
	}

	public void setLessonIdFuzzy(String lessonIdFuzzy){
		this.lessonIdFuzzy = lessonIdFuzzy;
	}

	public String getLessonIdFuzzy(){
		return this.lessonIdFuzzy;
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

	public void setCompleteTime(String completeTime){
		this.completeTime = completeTime;
	}

	public String getCompleteTime(){
		return this.completeTime;
	}

	public void setCompleteTimeStart(String completeTimeStart){
		this.completeTimeStart = completeTimeStart;
	}

	public String getCompleteTimeStart(){
		return this.completeTimeStart;
	}
	public void setCompleteTimeEnd(String completeTimeEnd){
		this.completeTimeEnd = completeTimeEnd;
	}

	public String getCompleteTimeEnd(){
		return this.completeTimeEnd;
	}

	public void setLastStudyTime(String lastStudyTime){
		this.lastStudyTime = lastStudyTime;
	}

	public String getLastStudyTime(){
		return this.lastStudyTime;
	}

	public void setLastStudyTimeStart(String lastStudyTimeStart){
		this.lastStudyTimeStart = lastStudyTimeStart;
	}

	public String getLastStudyTimeStart(){
		return this.lastStudyTimeStart;
	}
	public void setLastStudyTimeEnd(String lastStudyTimeEnd){
		this.lastStudyTimeEnd = lastStudyTimeEnd;
	}

	public String getLastStudyTimeEnd(){
		return this.lastStudyTimeEnd;
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
