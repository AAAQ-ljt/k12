package com.nexora.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 编程比赛 数据库操作接口
 */
public interface CodingContestMapper<T, P> extends BaseMapper<T, P> {

	Integer updateByContestId(@Param("bean") T t, @Param("contestId") String contestId);

	Integer deleteByContestId(@Param("contestId") String contestId);

	T selectByContestId(@Param("contestId") String contestId);
}
