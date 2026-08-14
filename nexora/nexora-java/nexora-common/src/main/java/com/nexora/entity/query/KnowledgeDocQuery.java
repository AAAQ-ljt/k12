package com.nexora.entity.query;

import java.util.Date;
import java.util.List;


/**
 * 知识库文档表参数
 */
public class KnowledgeDocQuery extends BaseParam {


	/**
	 * 文档ID
	 */
	private String docId;

	private String docIdFuzzy;

	/**
	 * 文档ID集合（批量查询）
	 */
	private List<String> docIds;

	/**
	 * 标题
	 */
	private String title;

	private String titleFuzzy;

	/**
	 * 学段【冗余：与ES metadata双写一致】
	 */
	private String stage;

	private String stageFuzzy;

	/**
	 * 知识点【冗余：检索过滤/管理筛选免join】
	 */
	private String knowledgePointId;

	private String knowledgePointIdFuzzy;

	/**
	 * 难度：1-3
	 */
	private Integer difficulty;

	/**
	 * 数据类型，默认KNOWLEDGE
	 */
	private String dataType;

	private String dataTypeFuzzy;

	/**
	 * 正文（Markdown）
	 */
	private String content;

	private String contentFuzzy;

	/**
	 * 来源：0手动维护 1资料解析
	 */
	private Integer sourceType;

	/**
	 * 来源资源ID（解析入库时回填），可空
	 */
	private String sourceResourceId;

	private String sourceResourceIdFuzzy;

	/**
	 * 向量状态：0待处理 1处理中 2已完成 3失败 4过期
	 */
	private Integer vectorStatus;

	/**
	 * 向量化失败时的错误原因，可空
	 */
	private String vectorError;

	private String vectorErrorFuzzy;

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
	private String createTime;

	private String createTimeStart;

	private String createTimeEnd;

	/**
	 * 更新时间
	 */
	private String updateTime;

	private String updateTimeStart;

	private String updateTimeEnd;


	public void setDocId(String docId){
		this.docId = docId;
	}

	public String getDocId(){
		return this.docId;
	}

	public void setDocIdFuzzy(String docIdFuzzy){
		this.docIdFuzzy = docIdFuzzy;
	}

	public String getDocIdFuzzy(){
		return this.docIdFuzzy;
	}

	public List<String> getDocIds() {
		return docIds;
	}

	public void setDocIds(List<String> docIds) {
		this.docIds = docIds;
	}

	public void setTitle(String title){
		this.title = title;
	}

	public String getTitle(){
		return this.title;
	}

	public void setTitleFuzzy(String titleFuzzy){
		this.titleFuzzy = titleFuzzy;
	}

	public String getTitleFuzzy(){
		return this.titleFuzzy;
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

	public void setKnowledgePointIdFuzzy(String knowledgePointIdFuzzy){
		this.knowledgePointIdFuzzy = knowledgePointIdFuzzy;
	}

	public String getKnowledgePointIdFuzzy(){
		return this.knowledgePointIdFuzzy;
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

	public void setDataTypeFuzzy(String dataTypeFuzzy){
		this.dataTypeFuzzy = dataTypeFuzzy;
	}

	public String getDataTypeFuzzy(){
		return this.dataTypeFuzzy;
	}

	public void setContent(String content){
		this.content = content;
	}

	public String getContent(){
		return this.content;
	}

	public void setContentFuzzy(String contentFuzzy){
		this.contentFuzzy = contentFuzzy;
	}

	public String getContentFuzzy(){
		return this.contentFuzzy;
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

	public void setSourceResourceIdFuzzy(String sourceResourceIdFuzzy){
		this.sourceResourceIdFuzzy = sourceResourceIdFuzzy;
	}

	public String getSourceResourceIdFuzzy(){
		return this.sourceResourceIdFuzzy;
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

	public void setVectorErrorFuzzy(String vectorErrorFuzzy){
		this.vectorErrorFuzzy = vectorErrorFuzzy;
	}

	public String getVectorErrorFuzzy(){
		return this.vectorErrorFuzzy;
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
