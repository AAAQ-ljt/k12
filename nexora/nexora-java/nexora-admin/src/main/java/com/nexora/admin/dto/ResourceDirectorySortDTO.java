package com.nexora.admin.dto;

import java.util.List;

/**
 * 目录同级排序入参
 */
public class ResourceDirectorySortDTO {

    /**
     * 上级目录ID
     */
    private String parentId;

    /**
     * 排序后的目录ID列表
     */
    private List<String> dirIds;

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public List<String> getDirIds() {
        return dirIds;
    }

    public void setDirIds(List<String> dirIds) {
        this.dirIds = dirIds;
    }
}
