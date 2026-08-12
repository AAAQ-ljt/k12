package com.nexora.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 课时学习进度表 数据库操作接口
 */
public interface CourseStudyLessonProgressMapper<T,P> extends BaseMapper<T,P> {

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
	 * 根据UserIdAndLessonId更新
	 */
	 Integer updateByUserIdAndLessonId(@Param("bean") T t,@Param("userId") String userId,@Param("lessonId") String lessonId);


	/**
	 * 根据UserIdAndLessonId删除
	 */
	 Integer deleteByUserIdAndLessonId(@Param("userId") String userId,@Param("lessonId") String lessonId);


	/**
	 * 根据UserIdAndLessonId获取对象
	 */
	 T selectByUserIdAndLessonId(@Param("userId") String userId,@Param("lessonId") String lessonId);


}
