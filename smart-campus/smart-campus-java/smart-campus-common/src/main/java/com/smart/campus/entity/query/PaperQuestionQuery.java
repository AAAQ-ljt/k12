package com.smart.campus.entity.query;

import java.math.BigDecimal;
import java.util.Date;


/**
 * 试卷题目编排表参数
 */
public class PaperQuestionQuery extends BaseParam {


	/**
	 * 主键ID
	 */
	private Integer id;

	/**
	 * 试卷ID，对应paper_info.paper_id
	 */
	private String paperId;

	private String paperIdFuzzy;

	/**
	 * 题目ID，对应question_info.question_id
	 */
	private Integer questionId;

	/**
	 * 试卷内题目分值
	 */
	private BigDecimal questionScore;

	/**
	 * 题目类型:1单选 2多选 3判断 4填空
	 */
	private Integer questionType;

	/**
	 * 分组 类型 1:分组 0:题目 
	 */
	private Integer sectionType;

	/**
	 * 分组名称，可为空，如单选题、多选题
	 */
	private String sectionName;

	private String sectionNameFuzzy;

	/**
	 * 父ID,如果是分组父级ID为0
	 */
	private Integer parentId;

	/**
	 * 试卷内排序值
	 */
	private Integer sortOrder;

	/**
	 * 题目快照，建议存JSON，包含标题、配图、选项、答案、答案解析等
	 */
	private String questionSnapshot;

	private String questionSnapshotFuzzy;

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


	public void setId(Integer id){
		this.id = id;
	}

	public Integer getId(){
		return this.id;
	}

	public void setPaperId(String paperId){
		this.paperId = paperId;
	}

	public String getPaperId(){
		return this.paperId;
	}

	public void setPaperIdFuzzy(String paperIdFuzzy){
		this.paperIdFuzzy = paperIdFuzzy;
	}

	public String getPaperIdFuzzy(){
		return this.paperIdFuzzy;
	}

	public void setQuestionId(Integer questionId){
		this.questionId = questionId;
	}

	public Integer getQuestionId(){
		return this.questionId;
	}

	public void setQuestionScore(BigDecimal questionScore){
		this.questionScore = questionScore;
	}

	public BigDecimal getQuestionScore(){
		return this.questionScore;
	}

	public void setQuestionType(Integer questionType){
		this.questionType = questionType;
	}

	public Integer getQuestionType(){
		return this.questionType;
	}

	public void setSectionType(Integer sectionType){
		this.sectionType = sectionType;
	}

	public Integer getSectionType(){
		return this.sectionType;
	}

	public void setSectionName(String sectionName){
		this.sectionName = sectionName;
	}

	public String getSectionName(){
		return this.sectionName;
	}

	public void setSectionNameFuzzy(String sectionNameFuzzy){
		this.sectionNameFuzzy = sectionNameFuzzy;
	}

	public String getSectionNameFuzzy(){
		return this.sectionNameFuzzy;
	}

	public void setParentId(Integer parentId){
		this.parentId = parentId;
	}

	public Integer getParentId(){
		return this.parentId;
	}

	public void setSortOrder(Integer sortOrder){
		this.sortOrder = sortOrder;
	}

	public Integer getSortOrder(){
		return this.sortOrder;
	}

	public void setQuestionSnapshot(String questionSnapshot){
		this.questionSnapshot = questionSnapshot;
	}

	public String getQuestionSnapshot(){
		return this.questionSnapshot;
	}

	public void setQuestionSnapshotFuzzy(String questionSnapshotFuzzy){
		this.questionSnapshotFuzzy = questionSnapshotFuzzy;
	}

	public String getQuestionSnapshotFuzzy(){
		return this.questionSnapshotFuzzy;
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
