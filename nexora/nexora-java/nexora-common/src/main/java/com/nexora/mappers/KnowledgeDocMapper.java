package com.nexora.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 知识库文档表 数据库操作接口
 */
public interface KnowledgeDocMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据DocId更新
	 */
	 Integer updateByDocId(@Param("bean") T t,@Param("docId") String docId);


	/**
	 * 根据DocId删除
	 */
	 Integer deleteByDocId(@Param("docId") String docId);


	/**
	 * 根据DocId获取对象
	 */
	 T selectByDocId(@Param("docId") String docId);


}
