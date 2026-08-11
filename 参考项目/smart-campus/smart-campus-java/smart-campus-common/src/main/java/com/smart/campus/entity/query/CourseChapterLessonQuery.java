package com.smart.campus.entity.query;



/**
 * 课程课时表参数
 */
public class CourseChapterLessonQuery extends BaseParam {


	/**
	 * 主键ID
	 */
	private String lessonId;

	private String lessonIdFuzzy;

	/**
	 * 所属课程ID
	 */
	private String courseId;

	private String courseIdFuzzy;

	/**
	 * 所属章节ID
	 */
	private String chapterId;

	private String chapterIdFuzzy;

	/**
	 * 课时名称
	 */
	private String lessonName;

	private String lessonNameFuzzy;

	/**
	 * 排序值
	 */
	private Integer sortOrder;


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

	public void setLessonName(String lessonName){
		this.lessonName = lessonName;
	}

	public String getLessonName(){
		return this.lessonName;
	}

	public void setLessonNameFuzzy(String lessonNameFuzzy){
		this.lessonNameFuzzy = lessonNameFuzzy;
	}

	public String getLessonNameFuzzy(){
		return this.lessonNameFuzzy;
	}

	public void setSortOrder(Integer sortOrder){
		this.sortOrder = sortOrder;
	}

	public Integer getSortOrder(){
		return this.sortOrder;
	}

}
