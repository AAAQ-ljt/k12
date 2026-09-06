package com.nexora.entity.query;

/**
 * 答题批阅列表参数（主观题人工批阅）
 */
public class PracticeReviewQuery extends BaseParam {

	/**
	 * 学生ID
	 */
	private String userId;

	/**
	 * 学生搜索（用户名/昵称/邮箱）
	 */
	private String studentFuzzy;

	/**
	 * 题目年级
	 */
	private String grade;

	/**
	 * 学科（知识点挂载）
	 */
	private String subject;

	/**
	 * 题型（4简答 5解答 6论述 7材料；空=全部主观题）
	 */
	private Integer questionType;

	/**
	 * 来源：3课时通关测验（试卷作答后续接入）
	 */
	private Integer source;

	/**
	 * 批阅状态：0待批阅 1已批阅
	 */
	private Integer reviewStatus;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getStudentFuzzy() {
		return studentFuzzy;
	}

	public void setStudentFuzzy(String studentFuzzy) {
		this.studentFuzzy = studentFuzzy;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Integer getQuestionType() {
		return questionType;
	}

	public void setQuestionType(Integer questionType) {
		this.questionType = questionType;
	}

	public Integer getSource() {
		return source;
	}

	public void setSource(Integer source) {
		this.source = source;
	}

	public Integer getReviewStatus() {
		return reviewStatus;
	}

	public void setReviewStatus(Integer reviewStatus) {
		this.reviewStatus = reviewStatus;
	}
}
