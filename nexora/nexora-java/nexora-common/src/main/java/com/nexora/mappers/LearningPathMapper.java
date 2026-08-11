package com.nexora.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 学习路径表 数据库操作接口
 */
public interface LearningPathMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据PathId更新
	 */
	 Integer updateByPathId(@Param("bean") T t,@Param("pathId") String pathId);


	/**
	 * 根据PathId删除
	 */
	 Integer deleteByPathId(@Param("pathId") String pathId);


	/**
	 * 根据PathId获取对象
	 */
	 T selectByPathId(@Param("pathId") String pathId);


}
