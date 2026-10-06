package com.nexora.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 编程比赛报名与成绩（同一行贯穿：报名 → 比赛 → 提交）
 */
@Data
public class CodingContestRecord implements Serializable {

	private Long recordId;

	private String contestId;

	private String userId;

	/** 学段【冗余快照】 */
	private String stage;

	/** 0已报名 1比赛中 2已提交 */
	private Integer status;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date enrollTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date startedAt;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date submittedAt;

	/** 得分 */
	private Integer score;

	/** 解出题数 */
	private Integer solvedCount;

	/** 赛题总数【快照】 */
	private Integer totalCount;

	/** 用时（秒） */
	private Integer duration;
}
