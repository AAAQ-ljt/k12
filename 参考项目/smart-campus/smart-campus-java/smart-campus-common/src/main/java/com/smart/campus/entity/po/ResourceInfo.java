package com.smart.campus.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.smart.campus.entity.enums.DateTimePatternEnum;
import com.smart.campus.utils.DateUtil;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;


/**
 * 教师资源信息表
 */
public class ResourceInfo implements Serializable {


    /**
     * 主键ID
     */
    private Integer resourceId;

    /**
     * 教师ID
     */
    private Integer teacherId;

    /**
     * 父节点ID，0表示根节点
     */
    private Integer parentId;

    /**
     * 节点类型:1目录 2资源
     */
    private Integer nodeType;

    /**
     * 名称，目录名或资源名
     */
    private String resourceName;

    /**
     * 资源类型:1 视频 2图片 3文档 4压缩包 5其他
     */
    private Integer resourceType;

    /**
     * 原始文件名，目录为空
     */
    private String fileName;

    /**
     * 文件后缀，目录为空
     */
    private String fileSuffix;

    /**
     * 文件大小，目录为0
     */
    private Long fileSize;

    /**
     * 文件存储路径，目录为空
     */
    private String filePath;

    /**
     * 封面图路径，可为空
     */
    private String coverPath;

    /**
     * 状态:1上传中 2转码中 3上传成功  4转码失败 5上传失败
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 持续时间
     */
    private Integer duration;


    public void setResourceId(Integer resourceId) {
        this.resourceId = resourceId;
    }

    public Integer getResourceId() {
        return this.resourceId;
    }

    public void setTeacherId(Integer teacherId) {
        this.teacherId = teacherId;
    }

    public Integer getTeacherId() {
        return this.teacherId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Integer getParentId() {
        return this.parentId;
    }

    public void setNodeType(Integer nodeType) {
        this.nodeType = nodeType;
    }

    public Integer getNodeType() {
        return this.nodeType;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getResourceName() {
        return this.resourceName;
    }

    public void setResourceType(Integer resourceType) {
        this.resourceType = resourceType;
    }

    public Integer getResourceType() {
        return this.resourceType;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileSuffix(String fileSuffix) {
        this.fileSuffix = fileSuffix;
    }

    public String getFileSuffix() {
        return this.fileSuffix;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Long getFileSize() {
        return this.fileSize;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }

    public String getCoverPath() {
        return this.coverPath;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getUpdateTime() {
        return this.updateTime;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getDuration() {
        return this.duration;
    }

    @Override
    public String toString() {
        return "主键ID:" + (resourceId == null ? "空" : resourceId) + "，教师ID:" + (teacherId == null ? "空" : teacherId) + "，父节点ID，0表示根节点:" + (parentId == null ? "空" :
                parentId) + "，节点类型:1目录 2资源:" + (nodeType == null ? "空" : nodeType) + "，名称，目录名或资源名:" + (resourceName == null ? "空" : resourceName) + "，资源类型:1课件 2视频 3文档 " +
                "4图片 5压缩包 9其他，目录为空:" + (resourceType == null ? "空" : resourceType) + "，原始文件名，目录为空:" + (fileName == null ? "空" : fileName) + "，文件后缀，目录为空:" + (fileSuffix == null ? "空" : fileSuffix) + "，文件大小，目录为0:" + (fileSize == null ? "空" : fileSize) + "，文件存储路径，目录为空:" + (filePath == null ? "空" : filePath) + "，封面图路径，可为空:" + (coverPath == null ? "空" : coverPath) + "，状态:1正常 0禁用:" + (status == null ? "空" : status) + "，创建时间:" + (createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + "，更新时间:" + (updateTime == null ? "空" : DateUtil.format(updateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + "，持续时间:" + (duration == null ? "空" : duration);
    }
}
