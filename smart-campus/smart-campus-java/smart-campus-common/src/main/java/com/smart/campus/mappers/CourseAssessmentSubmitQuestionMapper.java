package com.smart.campus.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 课程作业/考试学生答题明细表 数据库操作接口
 */
public interface CourseAssessmentSubmitQuestionMapper<T,P> extends BaseMapper<T,P> {

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
	 * 根据SubmitIdAndQuestionId更新
	 */
	 Integer updateBySubmitIdAndQuestionId(@Param("bean") T t,@Param("submitId") Long submitId,@Param("questionId") Integer questionId);


	/**
	 * 根据SubmitIdAndQuestionId删除
	 */
	 Integer deleteBySubmitIdAndQuestionId(@Param("submitId") Long submitId,@Param("questionId") Integer questionId);


	/**
	 * 根据SubmitIdAndQuestionId获取对象
	 */
	 T selectBySubmitIdAndQuestionId(@Param("submitId") Long submitId,@Param("questionId") Integer questionId);


}
