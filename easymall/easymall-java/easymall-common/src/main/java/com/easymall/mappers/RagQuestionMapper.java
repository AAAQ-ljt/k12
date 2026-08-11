package com.easymall.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * rag问题 数据库操作接口
 */
public interface RagQuestionMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据QuestionId更新
	 */
	 Integer updateByQuestionId(@Param("bean") T t,@Param("questionId") Integer questionId);


	/**
	 * 根据QuestionId删除
	 */
	 Integer deleteByQuestionId(@Param("questionId") Integer questionId);


	/**
	 * 根据QuestionId获取对象
	 */
	 T selectByQuestionId(@Param("questionId") Integer questionId);


}
