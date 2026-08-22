package com.nexora.entity.query;

/**
 * 学生学习行为记录 查询参数
 */
public class StudentLearningRecordQuery extends BaseParam {

    /**
     * 记录ID
     */
    private Long recordId;

    /**
     * 学生
     */
    private String userId;

    /**
     * 资源ID，可空
     */
    private String resourceId;

    /**
     * 课程ID，可空
     */
    private String courseId;

    /**
     * 课时ID，可空
     */
    private String lessonId;

    /**
     * VIEW/COMPLETE/PRACTICE/ANIMATION/PARSE
     */
    private String actionType;

    /**
     * 时长（秒）
     */
    private Integer duration;

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }
}