package com.nexora.entity.query;

import java.util.Date;
import java.util.List;


/**
 * 题目表参数
 */
public class QuestionInfoQuery extends BaseParam {


	/**
	 * 题目ID
	 */
	private String questionId;

	private String questionIdFuzzy;

	/**
	 * 题目ID集合（试卷预览批量查询）
	 */
	private List<String> questionIds;

	/**
	 * 知识点【冗余：出题/练习主筛选键】
	 */
	private String knowledgePointId;

	private String knowledgePointIdFuzzy;

	/**
	 * 学段【冗余：按学段抽题免join】
	 */
	private String stage;

	private String stageFuzzy;

	/**
	 * 年级【按年级抽题免join】
	 */
	private String grade;

	private String gradeFuzzy;

	/**
	 * 难度：1-3
	 */
	private Integer difficulty;

	/**
	 * 题型：0单选 1多选 2判断 3填空 4简答 5解答 6论述 7材料分析
	 */
	private Integer questionType;

	/**
	 * 题干
	 */
	private String title;

	private String titleFuzzy;

	/**
	 * 题目配图，关联resource_info.resource_id，多个逗号分隔，可空
	 */
	private String questionImage;

	private String questionImageFuzzy;

	/**
	 * 判断/填空答案；选择题答案在选项表
	 */
	private String answer;

	private String answerFuzzy;

	/**
	 * 解析
	 */
	private String analysis;

	private String analysisFuzzy;

	/**
	 * 来源：0管理员录入 1AI生成
	 */
	private Integer source;

	/**
	 * 审核：0待审核 1已上架 2已驳回
	 */
	private Integer auditStatus;

	/**
	 * 默认分值
	 */
	private Integer score;

	/**
	 * 状态：0停用 1启用
	 */
	private Integer status;

	/**
	 * 录入人，可空（AI生成为空）
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


	public void setQuestionId(String questionId){
		this.questionId = questionId;
	}

	public String getQuestionId(){
		return this.questionId;
	}

	public List<String> getQuestionIds() {
		return questionIds;
	}

	public void setQuestionIds(List<String> questionIds) {
		this.questionIds = questionIds;
	}

	public void setQuestionIdFuzzy(String questionIdFuzzy){
		this.questionIdFuzzy = questionIdFuzzy;
	}

	public String getQuestionIdFuzzy(){
		return this.questionIdFuzzy;
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

	public void setGrade(String grade){
		this.grade = grade;
	}

	public String getGrade(){
		return this.grade;
	}

	public void setGradeFuzzy(String gradeFuzzy){
		this.gradeFuzzy = gradeFuzzy;
	}

	public String getGradeFuzzy(){
		return this.gradeFuzzy;
	}

	public void setDifficulty(Integer difficulty){
		this.difficulty = difficulty;
	}

	public Integer getDifficulty(){
		return this.difficulty;
	}

	public void setQuestionType(Integer questionType){
		this.questionType = questionType;
	}

	public Integer getQuestionType(){
		return this.questionType;
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

	public void setQuestionImage(String questionImage){
		this.questionImage = questionImage;
	}

	public String getQuestionImage(){
		return this.questionImage;
	}

	public void setQuestionImageFuzzy(String questionImageFuzzy){
		this.questionImageFuzzy = questionImageFuzzy;
	}

	public String getQuestionImageFuzzy(){
		return this.questionImageFuzzy;
	}

	public void setAnswer(String answer){
		this.answer = answer;
	}

	public String getAnswer(){
		return this.answer;
	}

	public void setAnswerFuzzy(String answerFuzzy){
		this.answerFuzzy = answerFuzzy;
	}

	public String getAnswerFuzzy(){
		return this.answerFuzzy;
	}

	public void setAnalysis(String analysis){
		this.analysis = analysis;
	}

	public String getAnalysis(){
		return this.analysis;
	}

	public void setAnalysisFuzzy(String analysisFuzzy){
		this.analysisFuzzy = analysisFuzzy;
	}

	public String getAnalysisFuzzy(){
		return this.analysisFuzzy;
	}

	public void setSource(Integer source){
		this.source = source;
	}

	public Integer getSource(){
		return this.source;
	}

	public void setAuditStatus(Integer auditStatus){
		this.auditStatus = auditStatus;
	}

	public Integer getAuditStatus(){
		return this.auditStatus;
	}

	public void setScore(Integer score){
		this.score = score;
	}

	public Integer getScore(){
		return this.score;
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
