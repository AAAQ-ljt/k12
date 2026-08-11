package com.nexora.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 课时表 数据库操作接口
 */
public interface CourseChapterLessonMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据LessonId更新
	 */
	 Integer updateByLessonId(@Param("bean") T t,@Param("lessonId") String lessonId);


	/**
	 * 根据LessonId删除
	 */
	 Integer deleteByLessonId(@Param("lessonId") String lessonId);


	/**
	 * 根据LessonId获取对象
	 */
	 T selectByLessonId(@Param("lessonId") String lessonId);


}
