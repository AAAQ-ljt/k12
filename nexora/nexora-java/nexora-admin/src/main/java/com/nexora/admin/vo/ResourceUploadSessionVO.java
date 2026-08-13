package com.nexora.admin.vo;

import java.util.Set;

/**
 * 资源分片上传会话视图
 */
public class ResourceUploadSessionVO {

    private String uploadId;
    private String resourceId;
    private Integer shardSize;
    private Integer totalShards;
    private Set<Integer> uploadedShardIndexes;

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

    public Integer getShardSize() {
        return shardSize;
    }

    public void setShardSize(Integer shardSize) {
        this.shardSize = shardSize;
    }

    public Integer getTotalShards() {
        return totalShards;
    }

    public void setTotalShards(Integer totalShards) {
        this.totalShards = totalShards;
    }

    public Set<Integer> getUploadedShardIndexes() {
        return uploadedShardIndexes;
    }

    public void setUploadedShardIndexes(Set<Integer> uploadedShardIndexes) {
        this.uploadedShardIndexes = uploadedShardIndexes;
    }
}
