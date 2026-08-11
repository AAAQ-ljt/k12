package com.smart.campus.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AddFolderDTO {

    @NotNull(message = "父级目录不能为空")
    private Integer parentId;

    @NotBlank(message = "目录名称不能为空")
    private String resourceName;

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
}
