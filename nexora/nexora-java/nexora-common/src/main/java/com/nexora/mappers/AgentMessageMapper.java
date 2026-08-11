package com.nexora.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * AI消息表 数据库操作接口
 */
public interface AgentMessageMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据MessageId更新
	 */
	 Integer updateByMessageId(@Param("bean") T t,@Param("messageId") String messageId);


	/**
	 * 根据MessageId删除
	 */
	 Integer deleteByMessageId(@Param("messageId") String messageId);


	/**
	 * 根据MessageId获取对象
	 */
	 T selectByMessageId(@Param("messageId") String messageId);


}
