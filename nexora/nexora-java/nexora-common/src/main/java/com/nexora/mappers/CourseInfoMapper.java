package com.nexora.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 课程（教材）表 数据库操作接口
 */
public interface CourseInfoMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据CourseId更新
	 */
	 Integer updateByCourseId(@Param("bean") T t,@Param("courseId") String courseId);


	/**
	 * 根据CourseId删除
	 */
	 Integer deleteByCourseId(@Param("courseId") String courseId);


	/**
	 * 根据CourseId获取对象
	 */
	 T selectByCourseId(@Param("courseId") String courseId);


}
