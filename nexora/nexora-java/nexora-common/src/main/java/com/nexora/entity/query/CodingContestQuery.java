package com.nexora.entity.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 编程比赛查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CodingContestQuery extends BaseParam {

	private String contestId;

	private List<String> contestIds;

	private String stage;

	/** 0草稿 1已发布 2已结束 */
	private Integer status;

	private String titleFuzzy;

	/** 只查「已发布且未结束」的比赛（学生端可参加列表） */
	private Boolean openOnly;

	/** 按开始时间倒序 */
	private Boolean orderByStartTimeDesc;
}
