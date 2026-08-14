package com.nexora.admin.dto;

import java.util.List;

/**
 * 资源批量删除参数
 */
public class ResourceBatchDeleteDTO {

    private List<String> resourceIds;

    private List<String> dirIds;

    public List<String> getResourceIds() {
        return resourceIds;
    }

    public void setResourceIds(List<String> resourceIds) {
        this.resourceIds = resourceIds;
    }

    public List<String> getDirIds() {
        return dirIds;
    }

    public void setDirIds(List<String> dirIds) {
        this.dirIds = dirIds;
    }
}
