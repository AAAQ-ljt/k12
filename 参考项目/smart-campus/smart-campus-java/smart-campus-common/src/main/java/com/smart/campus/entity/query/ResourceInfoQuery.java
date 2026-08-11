package com.smart.campus.entity.query;

import java.util.List;


/**
 * 教师资源信息表参数
 */
public class ResourceInfoQuery extends BaseParam {


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

	private String resourceNameFuzzy;

	/**
	 * 资源类型:1课件 2视频 3文档 4图片 5压缩包 9其他，目录为空
	 */
	private Integer resourceType;

	/**
	 * 原始文件名，目录为空
	 */
	private String fileName;

	private String fileNameFuzzy;

	/**
	 * 文件后缀，目录为空
	 */
	private String fileSuffix;

	private String fileSuffixFuzzy;

	/**
	 * 文件大小，目录为0
	 */
	private Long fileSize;

	/**
	 * 文件存储路径，目录为空
	 */
	private String filePath;

	private String filePathFuzzy;

	/**
	 * 封面图路径，可为空
	 */
	private String coverPath;

	private String coverPathFuzzy;

	/**
	 * 状态:1正常 0禁用
	 */
	private Integer status;

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

	/**
	 * 持续时间
	 */
	private Integer duration;

    private List<Integer> resourceIdList;

    private List<Integer> parentIdList;

	public List<Integer> getResourceIdList() {
		return resourceIdList;
	}

	public void setResourceIdList(List<Integer> resourceIdList) {
		this.resourceIdList = resourceIdList;
	}

	public List<Integer> getParentIdList() {
		return parentIdList;
	}

	public void setParentIdList(List<Integer> parentIdList) {
		this.parentIdList = parentIdList;
	}

	public void setResourceId(Integer resourceId){
		this.resourceId = resourceId;
	}

	public Integer getResourceId(){
		return this.resourceId;
	}

	public void setTeacherId(Integer teacherId){
		this.teacherId = teacherId;
	}

	public Integer getTeacherId(){
		return this.teacherId;
	}

	public void setParentId(Integer parentId){
		this.parentId = parentId;
	}

	public Integer getParentId(){
		return this.parentId;
	}

	public void setNodeType(Integer nodeType){
		this.nodeType = nodeType;
	}

	public Integer getNodeType(){
		return this.nodeType;
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

	public void setResourceType(Integer resourceType){
		this.resourceType = resourceType;
	}

	public Integer getResourceType(){
		return this.resourceType;
	}

	public void setFileName(String fileName){
		this.fileName = fileName;
	}

	public String getFileName(){
		return this.fileName;
	}

	public void setFileNameFuzzy(String fileNameFuzzy){
		this.fileNameFuzzy = fileNameFuzzy;
	}

	public String getFileNameFuzzy(){
		return this.fileNameFuzzy;
	}

	public void setFileSuffix(String fileSuffix){
		this.fileSuffix = fileSuffix;
	}

	public String getFileSuffix(){
		return this.fileSuffix;
	}

	public void setFileSuffixFuzzy(String fileSuffixFuzzy){
		this.fileSuffixFuzzy = fileSuffixFuzzy;
	}

	public String getFileSuffixFuzzy(){
		return this.fileSuffixFuzzy;
	}

	public void setFileSize(Long fileSize){
		this.fileSize = fileSize;
	}

	public Long getFileSize(){
		return this.fileSize;
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

	public void setCoverPath(String coverPath){
		this.coverPath = coverPath;
	}

	public String getCoverPath(){
		return this.coverPath;
	}

	public void setCoverPathFuzzy(String coverPathFuzzy){
		this.coverPathFuzzy = coverPathFuzzy;
	}

	public String getCoverPathFuzzy(){
		return this.coverPathFuzzy;
	}

	public void setStatus(Integer status){
		this.status = status;
	}

	public Integer getStatus(){
		return this.status;
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

	public void setDuration(Integer duration){
		this.duration = duration;
	}

	public Integer getDuration(){
		return this.duration;
	}

}
