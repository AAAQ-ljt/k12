package com.smart.campus.entity.dto;

import jakarta.validation.constraints.NotNull;

public class MoveResourceDTO {

    @NotNull(message = "资源ID不能为空")
    private Integer resourceId;

    @NotNull(message = "目标目录不能为空")
    private Integer targetParentId;

    public Integer getResourceId() {
        return resourceId;
    }

    public void setResourceId(Integer resourceId) {
        this.resourceId = resourceId;
    }

    public Integer getTargetParentId() {
        return targetParentId;
    }

    public void setTargetParentId(Integer targetParentId) {
        this.targetParentId = targetParentId;
    }
}
