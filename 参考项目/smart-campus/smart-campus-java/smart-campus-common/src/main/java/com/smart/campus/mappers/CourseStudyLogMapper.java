package com.smart.campus.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 学生学习流水表 数据库操作接口
 */
public interface CourseStudyLogMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据Id更新
	 */
	 Integer updateById(@Param("bean") T t,@Param("id") Long id);


	/**
	 * 根据Id删除
	 */
	 Integer deleteById(@Param("id") Long id);


	/**
	 * 根据Id获取对象
	 */
	 T selectById(@Param("id") Long id);


	/**
	 * 根据SessionId更新
	 */
	 Integer updateBySessionId(@Param("bean") T t,@Param("sessionId") String sessionId);


	/**
	 * 根据SessionId删除
	 */
	 Integer deleteBySessionId(@Param("sessionId") String sessionId);


	/**
	 * 根据SessionId获取对象
	 */
	 T selectBySessionId(@Param("sessionId") String sessionId);


}
