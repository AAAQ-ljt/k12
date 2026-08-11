package com.smart.campus.entity.dto;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public class CourseExamDraftSaveDTO implements Serializable {

    @NotNull(message = "提交ID不能为空")
    private Long submitId;

    private String submitContent;

    private Integer usedSeconds;

    public Long getSubmitId() {
        return submitId;
    }

    public void setSubmitId(Long submitId) {
        this.submitId = submitId;
    }

    public String getSubmitContent() {
        return submitContent;
    }

    public void setSubmitContent(String submitContent) {
        this.submitContent = submitContent;
    }

    public Integer getUsedSeconds() {
        return usedSeconds;
    }

    public void setUsedSeconds(Integer usedSeconds) {
        this.usedSeconds = usedSeconds;
    }
}
