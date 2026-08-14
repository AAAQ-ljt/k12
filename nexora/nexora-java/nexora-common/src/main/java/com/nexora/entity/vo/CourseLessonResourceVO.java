package com.nexora.entity.vo;

import java.io.Serializable;
import java.util.Date;

/**
 * 课时资源视图（对外只暴露学习所需字段，不包含内部存储路径）
 */
public class CourseLessonResourceVO implements Serializable {

    /**
     * 关联ID
     */
    private Integer id;

    /**
     * 课时ID
     */
    private String lessonId;

    /**
     * 课程ID
     */
    private String courseId;

    /**
     * 资源ID
     */
    private String resourceId;

    /**
     * 资源名
     */
    private String resourceName;

    /**
     * 资源类型
     */
    private String resourceType;

    /**
     * 资源简介
     */
    private String description;

    /**
     * 封面
     */
    private String cover;

    /**
     * 音视频时长（秒）
     */
    private Integer duration;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private Date createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
