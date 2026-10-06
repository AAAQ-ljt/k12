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


	/**
	 * 学习人数 +1（加入课程时调用；原子自增，避免并发读改写丢更新）
	 */
	 Integer increaseStudyCount(@Param("courseId") String courseId);


	/**
	 * 学习人数 -1（退出课程时调用；不小于 0）
	 */
	 Integer decreaseStudyCount(@Param("courseId") String courseId);

}
