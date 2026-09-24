package com.nexora.dto;

/**
 * 绘本单页补画入参
 */
public class PictureBookPageFixRequest {

    /** 绘本资源ID */
    private String resourceId;

    /** 页码（0 开始） */
    private Integer page;

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }
}