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

	/** 该生是否已通关（二期由 coding_problem_record 提供，本期恒为 null） */
	private Boolean passed;
}
