package com.smart.campus.web.entity.dto.studyplan;

import jakarta.validation.constraints.NotNull;

public class StudyPlanItemStatusDTO {

    @NotNull(message = "计划明细ID不能为空")
    private Long itemId;

    @NotNull(message = "计划状态不能为空")
    private Integer status;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
