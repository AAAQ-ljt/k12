package com.nexora.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 编程比赛视图（含我的报名/成绩概览）
 */
@Data
public class CodingContestVO {

	private String contestId;

	private String title;

	private String stage;

	private String description;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date startTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date endTime;

	private Integer durationMinutes;

	private Integer allowAnswer;

	private Integer status;

	/** 赛题数与总分 */
	private Integer problemCount;

	private Integer totalScore;

	/** 我的状态：0未报名 1已报名 2比赛中 3已提交 */
	private Integer myStatus;

	private Integer myScore;

	private Integer mySolvedCount;

	/** 我的用时（秒） */
	private Integer myDuration;

	/** 已提交人数（排行榜规模，本期仅展示人数） */
	private Integer submitCount;

	/** 赛题列表（仅详情接口下发） */
	private List<CodingProblemVO> problems;
}
