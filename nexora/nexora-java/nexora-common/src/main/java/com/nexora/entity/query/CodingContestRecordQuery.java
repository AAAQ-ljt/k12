package com.nexora.entity.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 编程比赛报名/成绩查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CodingContestRecordQuery extends BaseParam {

	private Long recordId;

	private String contestId;

	private List<String> contestIds;

	private String userId;

	/** 0已报名 1比赛中 2已提交 */
	private Integer status;

	private String stage;

	/** 按得分降序（排行榜用） */
	private Boolean orderByScoreDesc;
}
