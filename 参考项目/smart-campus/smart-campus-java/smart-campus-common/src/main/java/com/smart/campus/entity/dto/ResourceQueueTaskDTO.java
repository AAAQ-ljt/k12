package com.smart.campus.entity.dto;

import com.smart.campus.entity.constants.Constants;

import java.io.Serializable;

public class ResourceQueueTaskDTO implements Serializable {

    public static final String TYPE_MERGE_UPLOAD = Constants.RESOURCE_TASK_TYPE_MERGE_UPLOAD;

    public static final String TYPE_REUPLOAD_RESOURCE = Constants.RESOURCE_TASK_TYPE_REUPLOAD_RESOURCE;

    public static final String TYPE_TRANSCODE_VIDEO = Constants.RESOURCE_TASK_TYPE_TRANSCODE_VIDEO;

    private String taskType;

    private String uploadId;

    private Integer resourceId;

    private String mergedTempPath;

    private String sourceFileName;

    private String oldFilePath;

    private String oldCoverPath;

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public Integer getResourceId() {
        return resourceId;
    }

    public void setResourceId(Integer resourceId) {
        this.resourceId = resourceId;
    }

    public String getMergedTempPath() {
        return mergedTempPath;
    }

    public void setMergedTempPath(String mergedTempPath) {
        this.mergedTempPath = mergedTempPath;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public void setSourceFileName(String sourceFileName) {
        this.sourceFileName = sourceFileName;
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
