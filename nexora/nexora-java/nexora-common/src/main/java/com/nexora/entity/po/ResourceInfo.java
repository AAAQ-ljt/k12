package com.nexora.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import com.nexora.entity.enums.DateTimePatternEnum;
import com.nexora.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 资源信息表
 */
public class ResourceInfo implements Serializable {


	/**
	 * 资源ID
	 */
	private String resourceId;

	/**
	 * 资源名
	 */
	private String resourceName;

	/**
	 * 类型：VIDEO/DOCUMENT/PPT/WORD/IMAGE/PICTURE_BOOK
	 */
	private String resourceType;

	/**
	 * 资源标签，多个逗号分隔
	 */
	private String tags;

	/**
	 * 资源简介
	 */
	private String description;

	/**
	 * 文件地址
	 */
	private String filePath;

	/**
	 * 文件大小（字节）
	 */
	private Long fileSize;

	/**
	 * 封面
	 */
	private String cover;

	/**
	 * 音视频时长（秒）
	 */
	private Integer duration;

	/**
	 * HLS转码产物地址
	 */
	private String hlsPath;

	/**
	 * 归属学段，可空
	 */
	private String stage;

	/**
	 * 关联知识点【冗余：recommendResource工具按知识点直查】
	 */
	private String knowledgePointId;

	/**
	 * 来源：0后台上传 1AI生成
	 */
	private Integer source;

	/**
	 * 状态：0处理中 1可用 2失败
	 */
	private Integer status;

	/**
	 * 上传人
	 */
	private Integer createBy;

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


	public void setResourceId(String resourceId){
		this.resourceId = resourceId;
	}

	public String getResourceId(){
		return this.resourceId;
	}

	public void setResourceName(String resourceName){
		this.resourceName = resourceName;
	}

	public String getResourceName(){
		return this.resourceName;
	}

	public void setResourceType(String resourceType){
		this.resourceType = resourceType;
	}

	public String getResourceType(){
		return this.resourceType;
	}

	public void setTags(String tags){
		this.tags = tags;
	}

	public String getTags(){
		return this.tags;
	}

	public void setDescription(String description){
		this.description = description;
	}

	public String getDescription(){
		return this.description;
	}

	public void setFilePath(String filePath){
		this.filePath = filePath;
	}

	public String getFilePath(){
		return this.filePath;
	}

	public void setFileSize(Long fileSize){
		this.fileSize = fileSize;
	}

	public Long getFileSize(){
		return this.fileSize;
	}

	public void setCover(String cover){
		this.cover = cover;
	}

	public String getCover(){
		return this.cover;
	}

	public void setDuration(Integer duration){
		this.duration = duration;
	}

	public Integer getDuration(){
		return this.duration;
	}

	public void setHlsPath(String hlsPath){
		this.hlsPath = hlsPath;
	}

	public String getHlsPath(){
		return this.hlsPath;
	}

	public void setStage(String stage){
		this.stage = stage;
	}

	public String getStage(){
		return this.stage;
	}

	public void setKnowledgePointId(String knowledgePointId){
		this.knowledgePointId = knowledgePointId;
	}

	public String getKnowledgePointId(){
		return this.knowledgePointId;
	}

	public void setSource(Integer source){
		this.source = source;
	}

	public Integer getSource(){
		return this.source;
	}

	public void setStatus(Integer status){
		this.status = status;
	}

	public Integer getStatus(){
		return this.status;
	}

	public void setCreateBy(Integer createBy){
		this.createBy = createBy;
	}

	public Integer getCreateBy(){
		return this.createBy;
	}

	public void setCreateTime(Date createTime){
		this.createTime = createTime;
	}

	public Date getCreateTime(){
		return this.createTime;
	}

	public void setUpdateTime(Date updateTime){
		this.updateTime = updateTime;
	}

	public Date getUpdateTime(){
		return this.updateTime;
	}

	@Override
	public String toString (){
		return "资源ID:"+(resourceId == null ? "空" : resourceId)+"，资源名:"+(resourceName == null ? "空" : resourceName)+"，类型：VIDEO/DOCUMENT/PPT/WORD/IMAGE/PICTURE_BOOK:"+(resourceType == null ? "空" : resourceType)+"，资源标签，多个逗号分隔:"+(tags == null ? "空" : tags)+"，资源简介:"+(description == null ? "空" : description)+"，文件地址:"+(filePath == null ? "空" : filePath)+"，文件大小（字节）:"+(fileSize == null ? "空" : fileSize)+"，封面:"+(cover == null ? "空" : cover)+"，音视频时长（秒）:"+(duration == null ? "空" : duration)+"，HLS转码产物地址:"+(hlsPath == null ? "空" : hlsPath)+"，归属学段，可空:"+(stage == null ? "空" : stage)+"，关联知识点【冗余：recommendResource工具按知识点直查】:"+(knowledgePointId == null ? "空" : knowledgePointId)+"，来源：0后台上传 1AI生成:"+(source == null ? "空" : source)+"，状态：0处理中 1可用 2失败:"+(status == null ? "空" : status)+"，上传人:"+(createBy == null ? "空" : createBy)+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，更新时间:"+(updateTime == null ? "空" : DateUtil.format(updateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
