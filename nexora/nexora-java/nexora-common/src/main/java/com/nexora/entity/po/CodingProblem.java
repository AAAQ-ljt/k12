package com.nexora.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 编程题（题库）
 */
@Data
public class CodingProblem implements Serializable {

	private String problemId;

	/** 学段：PRIMARY_HIGH/JUNIOR/SENIOR */
	private String stage;

	/** 年级（可空） */
	private String grade;

	/** 知识点（二期用于掌握度回写） */
	private String knowledgePointId;

	/** 难度：1入门 2基础 3进阶 4挑战 */
	private Integer difficulty;

	private String title;

	/** 一句话目标 */
	private String goal;

	/** 题目描述（Markdown） */
	private String description;

	/** 思路提示 */
	private String hint;

	/** 预置代码框架 */
	private String starterCode;

	/** 参考答案（不随列表下发） */
	private String referenceCode;

	/** 答案讲解 */
	private String solutionNotes;

	/** 判定：1关键词包含 2输出精确匹配 3正则 */
	private Integer judgeType;

	private String expectedOutput;

	/** 关键词，逗号分隔 */
	private String expectedKeywords;

	private String expectedPattern;

	/** 积分 */
	private Integer score;

	/** 预估时长（分钟） */
	private Integer estimateMinutes;

	/** 同难度内排序 */
	private Integer sort;

	private String language;

	/** 0下架 1上架 */
	private Integer status;

	private Integer createBy;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date updateTime;
}
