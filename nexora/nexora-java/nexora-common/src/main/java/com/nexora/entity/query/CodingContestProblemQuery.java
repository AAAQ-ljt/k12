package com.nexora.entity.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 编程比赛赛题查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CodingContestProblemQuery extends BaseParam {

	private Long id;

	private String contestId;

	private List<String> contestIds;

	private String problemId;
}
