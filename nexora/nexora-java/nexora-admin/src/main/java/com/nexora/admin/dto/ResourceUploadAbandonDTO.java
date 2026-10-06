package com.nexora.admin.dto;

/**
 * 放弃上传上报入参（页面关闭 / 刷新、分片重试耗尽时由前端上报）
 */
public class ResourceUploadAbandonDTO {

    /**
     * 上传会话ID
     */
    private String uploadId;

    /**
     * 资源ID
     */
    private String resourceId;

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
}
