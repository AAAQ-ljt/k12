package com.smart.campus.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RenameResourceDTO {

    @NotNull(message = "资源ID不能为空")
    private Integer resourceId;

    @NotBlank(message = "资源名称不能为空")
    private String resourceName;

    public Integer getResourceId() {
        return resourceId;
    }

    public void setResourceId(Integer resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
}
