package com.smart.campus.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 试卷题目编排表 数据库操作接口
 */
public interface PaperQuestionMapper<T,P> extends BaseMapper<T,P> {

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
	 * 根据PaperIdAndQuestionId更新
	 */
	 Integer updateByPaperIdAndQuestionId(@Param("bean") T t,@Param("paperId") String paperId,@Param("questionId") Integer questionId);


	/**
	 * 根据PaperIdAndQuestionId删除
	 */
	 Integer deleteByPaperIdAndQuestionId(@Param("paperId") String paperId,@Param("questionId") Integer questionId);


	/**
	 * 根据PaperIdAndQuestionId获取对象
	 */
	 T selectByPaperIdAndQuestionId(@Param("paperId") String paperId,@Param("questionId") Integer questionId);


}
