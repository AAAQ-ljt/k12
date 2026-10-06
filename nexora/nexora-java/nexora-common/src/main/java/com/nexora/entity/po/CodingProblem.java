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

	/** 输出要求（Markdown：必须打印什么/几行/小数位/单位/标点用英文半角），随题目下发 */
	private String outputSpec;

	/** 输出示例（等宽文本，用「另一组数据」演示格式，禁止使用本题数据以免泄题），随题目下发 */
	private String outputExample;

	private String expectedOutput;

	/** 关键词，逗号分隔 */
	private String expectedKeywords;

	private String expectedPattern;

	/** 数值容差：0否 1是（开启后两边都能解析为数字的行按数值比较，78.50 与 78.5 视为相同） */
	private Integer numericTolerant;

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
