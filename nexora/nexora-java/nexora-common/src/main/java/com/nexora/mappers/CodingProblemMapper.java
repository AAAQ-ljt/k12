package com.nexora.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 编程题库 数据库操作接口
 */
public interface CodingProblemMapper<T, P> extends BaseMapper<T, P> {

	Integer updateByProblemId(@Param("bean") T t, @Param("problemId") String problemId);

	Integer deleteByProblemId(@Param("problemId") String problemId);

	T selectByProblemId(@Param("problemId") String problemId);
}
