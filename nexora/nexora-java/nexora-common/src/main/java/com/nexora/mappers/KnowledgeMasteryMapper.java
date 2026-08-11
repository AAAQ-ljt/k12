package com.nexora.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 知识点掌握度表 数据库操作接口
 */
public interface KnowledgeMasteryMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据Id更新
	 */
	 Integer updateById(@Param("bean") T t,@Param("id") Integer id);


	/**
	 * 根据Id删除
	 */
	 Integer deleteById(@Param("id") Integer id);


	/**
	 * 根据Id获取对象
	 */
	 T selectById(@Param("id") Integer id);


	/**
	 * 根据UserIdAndKnowledgePointId更新
	 */
	 Integer updateByUserIdAndKnowledgePointId(@Param("bean") T t,@Param("userId") Integer userId,@Param("knowledgePointId") String knowledgePointId);


	/**
	 * 根据UserIdAndKnowledgePointId删除
	 */
	 Integer deleteByUserIdAndKnowledgePointId(@Param("userId") Integer userId,@Param("knowledgePointId") String knowledgePointId);


	/**
	 * 根据UserIdAndKnowledgePointId获取对象
	 */
	 T selectByUserIdAndKnowledgePointId(@Param("userId") Integer userId,@Param("knowledgePointId") String knowledgePointId);


}
