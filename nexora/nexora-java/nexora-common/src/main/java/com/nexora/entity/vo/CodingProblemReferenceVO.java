package com.nexora.entity.vo;

import lombok.Data;

/**
 * 编程题参考答案视图（仅在允许看答案时下发）
 */
@Data
public class CodingProblemReferenceVO {

	private String problemId;

	private String referenceCode;

	private String solutionNotes;

	/** 本次查看是否影响积分：比赛期间不允许看答案，练习模式看答案后只计 30% */
	private Boolean scoreDiscounted;

	/** 不允许查看的原因（如「比赛进行中不可查看答案」） */
	private String denyReason;
}
