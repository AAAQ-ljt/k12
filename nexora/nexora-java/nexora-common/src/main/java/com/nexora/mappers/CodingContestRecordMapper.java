package com.nexora.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 编程比赛报名与成绩 数据库操作接口
 */
public interface CodingContestRecordMapper<T, P> extends BaseMapper<T, P> {

	T selectByContestIdAndUserId(@Param("contestId") String contestId, @Param("userId") String userId);

	Integer updateByContestIdAndUserId(@Param("bean") T t, @Param("contestId") String contestId,
			@Param("userId") String userId);
}
