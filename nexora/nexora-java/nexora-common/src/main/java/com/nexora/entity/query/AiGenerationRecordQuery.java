package com.nexora.entity.query;

import java.util.Date;


/**
 * AI生成记录表参数
 */
public class AiGenerationRecordQuery extends BaseParam {


	/**
	 * 记录ID
	 */
	private String recordId;

	private String recordIdFuzzy;

	/**
	 * 学生，可空（管理员预置无学生）
	 */
	private Integer userId;

	/**
	 * 学段【冗余：预置绘本库按学段过滤】
	 */
	private String stage;

	private String stageFuzzy;

	/**
	 * 知识点，可空
	 */
	private String knowledgePointId;

	private String knowledgePointIdFuzzy;

	/**
	 * 类型：ANIMATION/PICTURE_BOOK/DRAW/PPT/WORD/CODE
	 */
	private String type;

	private String typeFuzzy;

	/**
	 * 标题
	 */
	private String title;

	private String titleFuzzy;

	/**
	 * 结构化内容JSON（SVG分步脚本/绘本分页等）
	 */
	private String content;

	private String contentFuzzy;

	/**
	 * 产物文件地址（Word/PPT/图片）
	 */
	private String fileUrl;

	private String fileUrlFuzzy;

	/**
	 * 封面
	 */
	private String coverUrl;

	private String coverUrlFuzzy;

	/**
	 * 来源：0学生生成 1管理员预置
	 */
	private Integer source;

	/**
	 * 状态：0生成中 1完成 2失败 3已发布
	 */
	private Integer status;

	/**
	 * 学生是否已保存到"我的"：0否 1是
	 */
	private Integer saved;

	/**
	 * 审核：0待审核 1通过 2驳回（动画审核流程）
	 */
	private Integer auditStatus;

	/**
	 * 管理员预置时记录操作人ID，可空
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


	public void setRecordId(String recordId){
		this.recordId = recordId;
	}

	public String getRecordId(){
		return this.recordId;
	}

	public void setRecordIdFuzzy(String recordIdFuzzy){
		this.recordIdFuzzy = recordIdFuzzy;
	}

	public String getRecordIdFuzzy(){
		return this.recordIdFuzzy;
	}

	public void setUserId(Integer userId){
		this.userId = userId;
	}

	public Integer getUserId(){
		return this.userId;
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

	public void setType(String type){
		this.type = type;
	}

	public String getType(){
		return this.type;
	}

	public void setTypeFuzzy(String typeFuzzy){
		this.typeFuzzy = typeFuzzy;
	}

	public String getTypeFuzzy(){
		return this.typeFuzzy;
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

	public void setFileUrl(String fileUrl){
		this.fileUrl = fileUrl;
	}

	public String getFileUrl(){
		return this.fileUrl;
	}

	public void setFileUrlFuzzy(String fileUrlFuzzy){
		this.fileUrlFuzzy = fileUrlFuzzy;
	}

	public String getFileUrlFuzzy(){
		return this.fileUrlFuzzy;
	}

	public void setCoverUrl(String coverUrl){
		this.coverUrl = coverUrl;
	}

	public String getCoverUrl(){
		return this.coverUrl;
	}

	public void setCoverUrlFuzzy(String coverUrlFuzzy){
		this.coverUrlFuzzy = coverUrlFuzzy;
	}

	public String getCoverUrlFuzzy(){
		return this.coverUrlFuzzy;
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

	public void setSaved(Integer saved){
		this.saved = saved;
	}

	public Integer getSaved(){
		return this.saved;
	}

	public void setAuditStatus(Integer auditStatus){
		this.auditStatus = auditStatus;
	}

	public Integer getAuditStatus(){
		return this.auditStatus;
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
