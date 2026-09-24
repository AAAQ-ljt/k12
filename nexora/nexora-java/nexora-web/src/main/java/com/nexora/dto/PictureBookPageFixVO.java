package com.nexora.dto;

/**
 * 绘本单页补画任务状态（Redis 持久化，前端 2s 轮询）
 */
public class PictureBookPageFixVO {

    /** 绘本资源ID */
    private String resourceId;

    /** 页码（0 开始） */
    private int page;

    /** RUNNING / COMPLETED / FAILED */
    private String status;

    /** 进度说明 / 失败原因 */
    private String message;

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}