package com.nexora.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 知识点表（领域中心） 数据库操作接口
 */
public interface KnowledgePointMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据KnowledgePointId更新
	 */
	 Integer updateByKnowledgePointId(@Param("bean") T t,@Param("knowledgePointId") String knowledgePointId);


	/**
	 * 根据KnowledgePointId删除
	 */
	 Integer deleteByKnowledgePointId(@Param("knowledgePointId") String knowledgePointId);


	/**
	 * 根据KnowledgePointId获取对象
	 */
	 T selectByKnowledgePointId(@Param("knowledgePointId") String knowledgePointId);


}
