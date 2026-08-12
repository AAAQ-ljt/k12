package com.nexora.service;

import java.util.List;

import com.nexora.entity.query.CourseStudyLessonProgressQuery;
import com.nexora.entity.po.CourseStudyLessonProgress;
import com.nexora.entity.vo.PaginationResultVO;


/**
 * 课时学习进度表 业务接口
 */
public interface CourseStudyLessonProgressService {

	/**
	 * 根据条件查询列表
	 */
	List<CourseStudyLessonProgress> findListByParam(CourseStudyLessonProgressQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(CourseStudyLessonProgressQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<CourseStudyLessonProgress> findListByPage(CourseStudyLessonProgressQuery param);

	/**
	 * 新增
	 */
	Integer add(CourseStudyLessonProgress bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<CourseStudyLessonProgress> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<CourseStudyLessonProgress> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(CourseStudyLessonProgress bean,CourseStudyLessonProgressQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(CourseStudyLessonProgressQuery param);

	/**
	 * 根据Id查询对象
	 */
	CourseStudyLessonProgress getCourseStudyLessonProgressById(Integer id);


	/**
	 * 根据Id修改
	 */
	Integer updateCourseStudyLessonProgressById(CourseStudyLessonProgress bean,Integer id);


	/**
	 * 根据Id删除
	 */
	Integer deleteCourseStudyLessonProgressById(Integer id);


	/**
	 * 根据UserIdAndLessonId查询对象
	 */
	CourseStudyLessonProgress getCourseStudyLessonProgressByUserIdAndLessonId(String userId,String lessonId);


	/**
	 * 根据UserIdAndLessonId修改
	 */
	Integer updateCourseStudyLessonProgressByUserIdAndLessonId(CourseStudyLessonProgress bean,String userId,String lessonId);


	/**
	 * 根据UserIdAndLessonId删除
	 */
	Integer deleteCourseStudyLessonProgressByUserIdAndLessonId(String userId,String lessonId);

}