package com.smart.campus.entity.query;



/**
 * 课时资源关联表参数
 */
public class CourseChapterLessonResourceQuery extends BaseParam {


	/**
	 * 主键ID
	 */
	private Integer id;

	/**
	 * 课时ID
	 */
	private String lessonId;

	private String lessonIdFuzzy;

	/**
	 * 资源ID，对应resource_info.resource_id或习题id
	 */
	private String resourceId;

	private String resourceIdFuzzy;

	/**
	 * 资源类型: 1视频 2课件 3作业
	 */
	private Integer resourceType;

	/**
	 * 是否主资源: 1是 0否
	 */
	private Integer isPrimary;

	/**
	 * 排序值
	 */
	private Integer sortOrder;


	public void setId(Integer id){
		this.id = id;
	}

	public Integer getId(){
		return this.id;
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

	public void setResourceId(String resourceId){
		this.resourceId = resourceId;
	}

	public String getResourceId(){
		return this.resourceId;
	}

	public void setResourceIdFuzzy(String resourceIdFuzzy){
		this.resourceIdFuzzy = resourceIdFuzzy;
	}

	public String getResourceIdFuzzy(){
		return this.resourceIdFuzzy;
	}

	public void setResourceType(Integer resourceType){
		this.resourceType = resourceType;
	}

	public Integer getResourceType(){
		return this.resourceType;
	}

	public void setIsPrimary(Integer isPrimary){
		this.isPrimary = isPrimary;
	}

	public Integer getIsPrimary(){
		return this.isPrimary;
	}

	public void setSortOrder(Integer sortOrder){
		this.sortOrder = sortOrder;
	}

	public Integer getSortOrder(){
		return this.sortOrder;
	}

}
