package com.smart.campus.entity.dto;

import java.io.Serializable;

public class ResourceUploadSessionDTO implements Serializable {

    private String uploadId;

    private Integer teacherId;

    private Integer resourceId;

    private Integer parentId;

    private String resourceName;

    private Integer resourceType;

    private String fileName;

    private Long fileSize;

    private Integer chunkCount;

    private Boolean reUpload;

    private Boolean mergeQueued;

    private String oldFilePath;

    private String oldCoverPath;

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public Integer getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Integer teacherId) {
        this.teacherId = teacherId;
    }

    public Integer getResourceId() {
        return resourceId;
    }

    public void setResourceId(Integer resourceId) {
        this.resourceId = resourceId;
    }

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

    public Integer getResourceType() {
        return resourceType;
    }

    public void setResourceType(Integer resourceType) {
        this.resourceType = resourceType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Integer getChunkCount() {
        return chunkCount;
    }

    public void setChunkCount(Integer chunkCount) {
        this.chunkCount = chunkCount;
    }

    public Boolean getReUpload() {
        return reUpload;
    }

    public void setReUpload(Boolean reUpload) {
        this.reUpload = reUpload;
    }

    public Boolean getMergeQueued() {
        return mergeQueued;
    }

    public void setMergeQueued(Boolean mergeQueued) {
        this.mergeQueued = mergeQueued;
    }

    public String getOldFilePath() {
        return oldFilePath;
    }

    public void setOldFilePath(String oldFilePath) {
        this.oldFilePath = oldFilePath;
    }

    public String getOldCoverPath() {
        return oldCoverPath;
    }

    public void setOldCoverPath(String oldCoverPath) {
        this.oldCoverPath = oldCoverPath;
    }
}
