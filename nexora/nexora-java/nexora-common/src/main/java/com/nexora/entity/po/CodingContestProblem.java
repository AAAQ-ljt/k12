package com.nexora.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 编程比赛赛题（比赛与题目的关联 + 顺序 + 分值覆盖）
 */
@Data
public class CodingContestProblem implements Serializable {

	private Long id;

	private String contestId;

	private String problemId;

	/** 赛题顺序 */
	private Integer sort;

	/** 覆盖分值（空=取题目积分） */
	private Integer scoreOverride;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;
}
