package com.nexora.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import com.nexora.entity.enums.DateTimePatternEnum;
import com.nexora.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * AI生成记录表
 */
public class AiGenerationRecord implements Serializable {


	/**
	 * 记录ID
	 */
	private String recordId;

	/**
	 * 学生，可空（管理员预置无学生）
	 */
	private Integer userId;

	/**
	 * 学段【冗余：预置绘本库按学段过滤】
	 */
	private String stage;

	/**
	 * 知识点，可空
	 */
	private String knowledgePointId;

	/**
	 * 类型：ANIMATION/PICTURE_BOOK/DRAW/PPT/WORD/CODE
	 */
	private String type;

	/**
	 * 标题
	 */
	private String title;

	/**
	 * 结构化内容JSON（SVG分步脚本/绘本分页等）
	 */
	private String content;

	/**
	 * 产物文件地址（Word/PPT/图片）
	 */
	private String fileUrl;

	/**
	 * 封面
	 */
	private String coverUrl;

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
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	/**
	 * 更新时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date updateTime;


	public void setRecordId(String recordId){
		this.recordId = recordId;
	}

	public String getRecordId(){
		return this.recordId;
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

	public void setKnowledgePointId(String knowledgePointId){
		this.knowledgePointId = knowledgePointId;
	}

	public String getKnowledgePointId(){
		return this.knowledgePointId;
	}

	public void setType(String type){
		this.type = type;
	}

	public String getType(){
		return this.type;
	}

	public void setTitle(String title){
		this.title = title;
	}

	public String getTitle(){
		return this.title;
	}

	public void setContent(String content){
		this.content = content;
	}

	public String getContent(){
		return this.content;
	}

	public void setFileUrl(String fileUrl){
		this.fileUrl = fileUrl;
	}

	public String getFileUrl(){
		return this.fileUrl;
	}

	public void setCoverUrl(String coverUrl){
		this.coverUrl = coverUrl;
	}

	public String getCoverUrl(){
		return this.coverUrl;
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
		return "记录ID:"+(recordId == null ? "空" : recordId)+"，学生，可空（管理员预置无学生）:"+(userId == null ? "空" : userId)+"，学段【冗余：预置绘本库按学段过滤】:"+(stage == null ? "空" : stage)+"，知识点，可空:"+(knowledgePointId == null ? "空" : knowledgePointId)+"，类型：ANIMATION/PICTURE_BOOK/DRAW/PPT/WORD/CODE:"+(type == null ? "空" : type)+"，标题:"+(title == null ? "空" : title)+"，结构化内容JSON（SVG分步脚本/绘本分页等）:"+(content == null ? "空" : content)+"，产物文件地址（Word/PPT/图片）:"+(fileUrl == null ? "空" : fileUrl)+"，封面:"+(coverUrl == null ? "空" : coverUrl)+"，来源：0学生生成 1管理员预置:"+(source == null ? "空" : source)+"，状态：0生成中 1完成 2失败 3已发布:"+(status == null ? "空" : status)+"，学生是否已保存到\"我的\"：0否 1是:"+(saved == null ? "空" : saved)+"，审核：0待审核 1通过 2驳回（动画审核流程）:"+(auditStatus == null ? "空" : auditStatus)+"，管理员预置时记录操作人ID，可空:"+(createBy == null ? "空" : createBy)+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，更新时间:"+(updateTime == null ? "空" : DateUtil.format(updateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
