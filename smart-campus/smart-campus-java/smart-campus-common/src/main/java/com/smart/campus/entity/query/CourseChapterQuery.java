package com.smart.campus.entity.query;



/**
 * 课程章节表参数
 */
public class CourseChapterQuery extends BaseParam {


	/**
	 * 主键ID
	 */
	private String chapterId;

	private String chapterIdFuzzy;

	/**
	 * 所属课程ID
	 */
	private String courseId;

	private String courseIdFuzzy;

	/**
	 * 章节名称
	 */
	private String chapterName;

	private String chapterNameFuzzy;

	/**
	 * 章节说明
	 */
	private String description;

	private String descriptionFuzzy;

	/**
	 * 排序值
	 */
	private Integer sortOrder;


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

	public void setChapterName(String chapterName){
		this.chapterName = chapterName;
	}

	public String getChapterName(){
		return this.chapterName;
	}

	public void setChapterNameFuzzy(String chapterNameFuzzy){
		this.chapterNameFuzzy = chapterNameFuzzy;
	}

	public String getChapterNameFuzzy(){
		return this.chapterNameFuzzy;
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

	public void setSortOrder(Integer sortOrder){
		this.sortOrder = sortOrder;
	}

	public Integer getSortOrder(){
		return this.sortOrder;
	}

}
