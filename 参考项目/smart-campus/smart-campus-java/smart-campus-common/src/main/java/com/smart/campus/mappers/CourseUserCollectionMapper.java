package com.smart.campus.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 课程收藏 数据库操作接口
 */
public interface CourseUserCollectionMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据CourseIdAndUserId更新
	 */
	 Integer updateByCourseIdAndUserId(@Param("bean") T t,@Param("courseId") String courseId,@Param("userId") Integer userId);


	/**
	 * 根据CourseIdAndUserId删除
	 */
	 Integer deleteByCourseIdAndUserId(@Param("courseId") String courseId,@Param("userId") Integer userId);


	/**
	 * 根据CourseIdAndUserId获取对象
	 */
	 T selectByCourseIdAndUserId(@Param("courseId") String courseId,@Param("userId") Integer userId);


}
