package com.nexora.admin.dto;

import java.util.List;

/**
 * 课时绑定资源入参
 */
public class LessonResourceBindDTO {

    /**
     * 课时ID
     */
    private String lessonId;

    /**
     * 资源ID列表
     */
    private List<String> resourceIds;

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public List<String> getResourceIds() {
        return resourceIds;
    }

    public void setResourceIds(List<String> resourceIds) {
        this.resourceIds = resourceIds;
    }
}
