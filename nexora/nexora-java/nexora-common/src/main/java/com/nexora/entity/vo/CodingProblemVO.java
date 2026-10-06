package com.nexora.entity.vo;

import lombok.Data;

/**
 * 学生端编程题视图：**不下发参考答案与讲解**（答案走单独接口并按比赛规则校验）。
 */
@Data
public class CodingProblemVO {

	private String problemId;

	private String stage;

	private Integer difficulty;

	private String title;

	private String goal;

	private String description;

	private String hint;

	private String starterCode;

	private Integer score;

	private Integer estimateMinutes;

	private Integer sort;

	private String language;

	private String knowledgePointId;

	/** 输出要求（Markdown：必须打印什么/几行/小数位/单位/标点用英文半角），随题目下发 */
	private String outputSpec;

	/** 输出示例（等宽文本，用另一组数据演示格式，不含本题答案） */
	private String outputExample;

	/** 判定方式：1关键词包含 2输出精确匹配 3正则（只下发给前端做「判定口径」说明，预期值不下发） */
	private Integer judgeType;

	/** 数值容差：1 表示纯数值行按数值比较（78.5 与 78.50 视为相同） */
	private Integer numericTolerant;

	/** 该生是否已通关（二期由 coding_problem_record 提供，本期恒为 null） */
	private Boolean passed;
}
