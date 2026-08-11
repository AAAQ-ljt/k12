package com.smart.campus.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 用户消息收件表 数据库操作接口
 */
public interface MessageUserMapper<T,P> extends BaseMapper<T,P> {

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
	 * 根据MessageIdAndUserId更新
	 */
	 Integer updateByMessageIdAndUserId(@Param("bean") T t,@Param("messageId") Long messageId,@Param("userId") Integer userId);


	/**
	 * 根据MessageIdAndUserId删除
	 */
	 Integer deleteByMessageIdAndUserId(@Param("messageId") Long messageId,@Param("userId") Integer userId);


	/**
	 * 根据MessageIdAndUserId获取对象
	 */
	 T selectByMessageIdAndUserId(@Param("messageId") Long messageId,@Param("userId") Integer userId);


}
