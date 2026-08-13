package com.nexora.admin.dto;

import java.util.List;

/**
 * 文件批量转移目录入参
 */
public class ResourceMoveDTO {

    /**
     * 资源ID列表
     */
    private List<String> resourceIds;

    /**
     * 目标目录ID
     */
    private String directoryId;

    public List<String> getResourceIds() {
        return resourceIds;
    }

    public void setResourceIds(List<String> resourceIds) {
        this.resourceIds = resourceIds;
    }

    public String getDirectoryId() {
        return directoryId;
    }

    public void setDirectoryId(String directoryId) {
        this.directoryId = directoryId;
    }
}
