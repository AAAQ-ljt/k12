package com.nexora.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 编程比赛报名/成绩视图（个人中心与排行榜共用）
 */
@Data
public class CodingContestRecordVO {

	private Long recordId;

	private String contestId;

	private String contestTitle;

	private String stage;

	private String userId;

	/** 0已报名 1比赛中 2已提交 */
	private Integer status;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date enrollTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date submittedAt;

	private Integer score;

	private Integer solvedCount;

	private Integer totalCount;

	/** 用时（秒） */
	private Integer duration;

	/** 名次（排行榜接口下发） */
	private Integer rank;
}
