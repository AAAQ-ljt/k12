package com.nexora.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * AI会话表 数据库操作接口
 */
public interface AgentSessionMapper<T,P> extends BaseMapper<T,P> {

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
