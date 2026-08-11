package com.smart.campus.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 学生课程学习进度表 数据库操作接口
 */
public interface CourseStudyProgressMapper<T,P> extends BaseMapper<T,P> {

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
	 * 根据UserIdAndCourseId更新
	 */
	 Integer updateByUserIdAndCourseId(@Param("bean") T t,@Param("userId") Integer userId,@Param("courseId") String courseId);


	/**
	 * 根据UserIdAndCourseId删除
	 */
	 Integer deleteByUserIdAndCourseId(@Param("userId") Integer userId,@Param("courseId") String courseId);


	/**
	 * 根据UserIdAndCourseId获取对象
	 */
	 T selectByUserIdAndCourseId(@Param("userId") Integer userId,@Param("courseId") String courseId);


}
