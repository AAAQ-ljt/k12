package com.nexora.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 编程比赛赛题 数据库操作接口
 */
public interface CodingContestProblemMapper<T, P> extends BaseMapper<T, P> {

	/** 清空某场比赛的赛题（重新编排前调用） */
	Integer deleteByContestId(@Param("contestId") String contestId);
}
