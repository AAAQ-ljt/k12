package com.nexora.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 路径节点表 数据库操作接口
 */
public interface LearningPathItemMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据ItemId更新
	 */
	 Integer updateByItemId(@Param("bean") T t,@Param("itemId") String itemId);


	/**
	 * 根据ItemId删除
	 */
	 Integer deleteByItemId(@Param("itemId") String itemId);


	/**
	 * 根据ItemId获取对象
	 */
	 T selectByItemId(@Param("itemId") String itemId);


}
