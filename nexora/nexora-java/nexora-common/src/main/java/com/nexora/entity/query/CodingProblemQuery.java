package com.nexora.entity.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 编程题查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CodingProblemQuery extends BaseParam {

	private String problemId;

	private List<String> problemIds;

	private String stage;

	/** 难度：1入门 2基础 3进阶 4挑战 */
	private Integer difficulty;

	private Integer score;

	/** 0下架 1上架 */
	private Integer status;

	private String language;

	private String knowledgePointId;

	/** 标题模糊 */
	private String titleFuzzy;

	/** 标题或目标模糊（学生端搜索用） */
	private String keywordFuzzy;

	/** 按难度升序、同难度按 sort 升序 */
	private Boolean orderByDifficulty;
}
