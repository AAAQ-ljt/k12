package com.nexora.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 课程学习进度表 数据库操作接口
 */
public interface CourseStudyProgressMapper<T,P> extends BaseMapper<T,P> {

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
	 * 根据UserIdAndCourseId更新
	 */
	 Integer updateByUserIdAndCourseId(@Param("bean") T t,@Param("userId") String userId,@Param("courseId") String courseId);


	/**
	 * 根据UserIdAndCourseId删除
	 */
	 Integer deleteByUserIdAndCourseId(@Param("userId") String userId,@Param("courseId") String courseId);


	/**
	 * 根据UserIdAndCourseId获取对象
	 */
	 T selectByUserIdAndCourseId(@Param("userId") String userId,@Param("courseId") String courseId);


}
