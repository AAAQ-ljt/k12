package com.nexora.entity.po;

import java.util.Date;
import com.nexora.entity.enums.DateTimePatternEnum;
import com.nexora.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 知识库文档表
 */
public class KnowledgeDoc implements Serializable {


	/**
	 * 文档ID
	 */
	private String docId;

	/**
	 * 标题
	 */
	private String title;

	/**
	 * 学段【冗余：与ES metadata双写一致】
	 */
	private String stage;

	/**
	 * 知识点【冗余：检索过滤/管理筛选免join】
	 */
	private String knowledgePointId;

	/**
	 * 归属用户ID；NULL=官方知识库，非空=学生个人知识库
	 */
	private String ownerId;

	/**
	 * 难度：1-3
	 */
	private Integer difficulty;

	/**
	 * 数据类型，默认KNOWLEDGE
	 */
	private String dataType;

	/**
	 * 正文（Markdown）
	 */
	private String content;

	/**
	 * 来源：0手动维护 1资料解析
	 */
	private Integer sourceType;

	/**
	 * 来源资源ID（解析入库时回填），可空
	 */
	private String sourceResourceId;

	/**
	 * 资料链接（超链接文档），可空
	 */
	private String sourceUrl;

	/**
	 * 向量状态：0待处理 1处理中 2已完成 3失败 4过期
	 */
	private Integer vectorStatus;

	/**
	 * 向量化失败时的错误原因，可空
	 */
	private String vectorError;

	/**
	 * 入库分块数
	 */
	private Integer chunkCount;

	/**
	 * 状态：0下架 1上架
	 */
	private Integer status;

	/**
	 * 维护人
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


	public void setDocId(String docId){
		this.docId = docId;
	}

	public String getDocId(){
		return this.docId;
	}

	public void setTitle(String title){
		this.title = title;
	}

	public String getTitle(){
		return this.title;
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

	public void setOwnerId(String ownerId){
		this.ownerId = ownerId;
	}

	public String getOwnerId(){
		return this.ownerId;
	}

	public void setDifficulty(Integer difficulty){
		this.difficulty = difficulty;
	}

	public Integer getDifficulty(){
		return this.difficulty;
	}

	public void setDataType(String dataType){
		this.dataType = dataType;
	}

	public String getDataType(){
		return this.dataType;
	}

	public void setContent(String content){
		this.content = content;
	}

	public String getContent(){
		return this.content;
	}

	public void setSourceType(Integer sourceType){
		this.sourceType = sourceType;
	}

	public Integer getSourceType(){
		return this.sourceType;
	}

	public void setSourceResourceId(String sourceResourceId){
		this.sourceResourceId = sourceResourceId;
	}

	public String getSourceResourceId(){
		return this.sourceResourceId;
	}

	public void setSourceUrl(String sourceUrl){
		this.sourceUrl = sourceUrl;
	}

	public String getSourceUrl(){
		return this.sourceUrl;
	}

	public void setVectorStatus(Integer vectorStatus){
		this.vectorStatus = vectorStatus;
	}

	public Integer getVectorStatus(){
		return this.vectorStatus;
	}

	public void setVectorError(String vectorError){
		this.vectorError = vectorError;
	}

	public String getVectorError(){
		return this.vectorError;
	}

	public void setChunkCount(Integer chunkCount){
		this.chunkCount = chunkCount;
	}

	public Integer getChunkCount(){
		return this.chunkCount;
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
		return "文档ID:"+(docId == null ? "空" : docId)+"，标题:"+(title == null ? "空" : title)+"，学段【冗余：与ES metadata双写一致】:"+(stage == null ? "空" : stage)+"，知识点【冗余：检索过滤/管理筛选免join】:"+(knowledgePointId == null ? "空" : knowledgePointId)+"，难度：1-3:"+(difficulty == null ? "空" : difficulty)+"，数据类型，默认KNOWLEDGE:"+(dataType == null ? "空" : dataType)+"，正文（Markdown）:"+(content == null ? "空" : content)+"，来源：0手动维护 1资料解析:"+(sourceType == null ? "空" : sourceType)+"，来源资源ID（解析入库时回填），可空:"+(sourceResourceId == null ? "空" : sourceResourceId)+"，资料链接（超链接文档），可空:"+(sourceUrl == null ? "空" : sourceUrl)+"，向量状态：0待处理 1处理中 2已完成 3失败 4过期:"+(vectorStatus == null ? "空" : vectorStatus)+"，向量化失败时的错误原因，可空:"+(vectorError == null ? "空" : vectorError)+"，入库分块数:"+(chunkCount == null ? "空" : chunkCount)+"，状态：0下架 1上架:"+(status == null ? "空" : status)+"，维护人:"+(createBy == null ? "空" : createBy)+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，更新时间:"+(updateTime == null ? "空" : DateUtil.format(updateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
