package com.nexora.entity.query;

import java.util.Date;
import java.util.List;


/**
 * 资源信息表参数
 */
public class ResourceInfoQuery extends BaseParam {


	/**
	 * 资源ID
	 */
	private String resourceId;

	private String resourceIdFuzzy;

	/**
	 * 资源ID集合（批量查询）
	 */
	private List<String> resourceIds;

	/**
	 * 资源名
	 */
	private String resourceName;

	private String resourceNameFuzzy;

	/**
	 * 类型：VIDEO/DOCUMENT/PPT/WORD/IMAGE/PICTURE_BOOK
	 */
	private String resourceType;

	private String resourceTypeFuzzy;

	/**
	 * 资源标签，多个逗号分隔
	 */
	private String tags;

	private String tagsFuzzy;

	/**
	 * 资源简介
	 */
	private String description;

	private String descriptionFuzzy;

	/**
	 * 文件地址
	 */
	private String filePath;

	private String filePathFuzzy;

	/**
	 * 文件大小（字节）
	 */
	private Long fileSize;

	/**
	 * 封面
	 */
	private String cover;

	private String coverFuzzy;

	/**
	 * 音视频时长（秒）
	 */
	private Integer duration;

	/**
	 * HLS转码产物地址
	 */
	private String hlsPath;

	private String hlsPathFuzzy;

	/**
	 * 归属用户ID；NULL=管理端公共资源
	 */
	private String ownerId;

	/**
	 * 仅查询管理端公共资源（owner_id IS NULL）
	 */
	private Boolean ownerIdNull;

	/**
	 * 归属学段，可空
	 */
	private String stage;

	private String stageFuzzy;

	/**
	 * 关联知识点【冗余：recommendResource工具按知识点直查】
	 */
	private String knowledgePointId;

	private String knowledgePointIdFuzzy;

	/**
	 * 所属资源目录ID
	 */
	private String directoryId;

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
	private String createTime;

	private String createTimeStart;

	private String createTimeEnd;

	/**
	 * 更新时间
	 */
	private String updateTime;

	private String updateTimeStart;

	private String updateTimeEnd;


	public void setResourceId(String resourceId){
		this.resourceId = resourceId;
	}

	public String getResourceId(){
		return this.resourceId;
	}

	public void setResourceIdFuzzy(String resourceIdFuzzy){
		this.resourceIdFuzzy = resourceIdFuzzy;
	}

	public String getResourceIdFuzzy(){
		return this.resourceIdFuzzy;
	}

	public List<String> getResourceIds() {
		return resourceIds;
	}

	public void setResourceIds(List<String> resourceIds) {
		this.resourceIds = resourceIds;
	}

	public void setResourceName(String resourceName){
		this.resourceName = resourceName;
	}

	public String getResourceName(){
		return this.resourceName;
	}

	public void setResourceNameFuzzy(String resourceNameFuzzy){
		this.resourceNameFuzzy = resourceNameFuzzy;
	}

	public String getResourceNameFuzzy(){
		return this.resourceNameFuzzy;
	}

	public void setResourceType(String resourceType){
		this.resourceType = resourceType;
	}

	public String getResourceType(){
		return this.resourceType;
	}

	public void setResourceTypeFuzzy(String resourceTypeFuzzy){
		this.resourceTypeFuzzy = resourceTypeFuzzy;
	}

	public String getResourceTypeFuzzy(){
		return this.resourceTypeFuzzy;
	}

	public void setTags(String tags){
		this.tags = tags;
	}

	public String getTags(){
		return this.tags;
	}

	public void setTagsFuzzy(String tagsFuzzy){
		this.tagsFuzzy = tagsFuzzy;
	}

	public String getTagsFuzzy(){
		return this.tagsFuzzy;
	}

	public void setDescription(String description){
		this.description = description;
	}

	public String getDescription(){
		return this.description;
	}

	public void setDescriptionFuzzy(String descriptionFuzzy){
		this.descriptionFuzzy = descriptionFuzzy;
	}

	public String getDescriptionFuzzy(){
		return this.descriptionFuzzy;
	}

	public void setFilePath(String filePath){
		this.filePath = filePath;
	}

	public String getFilePath(){
		return this.filePath;
	}

	public void setFilePathFuzzy(String filePathFuzzy){
		this.filePathFuzzy = filePathFuzzy;
	}

	public String getFilePathFuzzy(){
		return this.filePathFuzzy;
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

	public void setCoverFuzzy(String coverFuzzy){
		this.coverFuzzy = coverFuzzy;
	}

	public String getCoverFuzzy(){
		return this.coverFuzzy;
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

	public void setHlsPathFuzzy(String hlsPathFuzzy){
		this.hlsPathFuzzy = hlsPathFuzzy;
	}

	public String getHlsPathFuzzy(){
		return this.hlsPathFuzzy;
	}

	public void setOwnerId(String ownerId){
		this.ownerId = ownerId;
	}

	public String getOwnerId(){
		return this.ownerId;
	}

	public void setOwnerIdNull(Boolean ownerIdNull){
		this.ownerIdNull = ownerIdNull;
	}

	public Boolean getOwnerIdNull(){
		return this.ownerIdNull;
	}

	public void setStage(String stage){
		this.stage = stage;
	}

	public String getStage(){
		return this.stage;
	}

	public void setStageFuzzy(String stageFuzzy){
		this.stageFuzzy = stageFuzzy;
	}

	public String getStageFuzzy(){
		return this.stageFuzzy;
	}

	public void setKnowledgePointId(String knowledgePointId){
		this.knowledgePointId = knowledgePointId;
	}

	public String getKnowledgePointId(){
		return this.knowledgePointId;
	}

	public void setDirectoryId(String directoryId){
		this.directoryId = directoryId;
	}

	public String getDirectoryId(){
		return this.directoryId;
	}

	public void setKnowledgePointIdFuzzy(String knowledgePointIdFuzzy){
		this.knowledgePointIdFuzzy = knowledgePointIdFuzzy;
	}

	public String getKnowledgePointIdFuzzy(){
		return this.knowledgePointIdFuzzy;
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

	public void setCreateTime(String createTime){
		this.createTime = createTime;
	}

	public String getCreateTime(){
		return this.createTime;
	}

	public void setCreateTimeStart(String createTimeStart){
		this.createTimeStart = createTimeStart;
	}

	public String getCreateTimeStart(){
		return this.createTimeStart;
	}
	public void setCreateTimeEnd(String createTimeEnd){
		this.createTimeEnd = createTimeEnd;
	}

	public String getCreateTimeEnd(){
		return this.createTimeEnd;
	}

	public void setUpdateTime(String updateTime){
		this.updateTime = updateTime;
	}

	public String getUpdateTime(){
		return this.updateTime;
	}

	public void setUpdateTimeStart(String updateTimeStart){
		this.updateTimeStart = updateTimeStart;
	}

	public String getUpdateTimeStart(){
		return this.updateTimeStart;
	}
	public void setUpdateTimeEnd(String updateTimeEnd){
		this.updateTimeEnd = updateTimeEnd;
	}

	public String getUpdateTimeEnd(){
		return this.updateTimeEnd;
	}

}
