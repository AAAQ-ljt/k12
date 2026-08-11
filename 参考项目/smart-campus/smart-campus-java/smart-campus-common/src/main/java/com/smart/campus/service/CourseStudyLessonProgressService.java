package com.smart.campus.service;

import java.util.List;

import com.smart.campus.entity.query.CourseStudyLessonProgressQuery;
import com.smart.campus.entity.po.CourseStudyLessonProgress;
import com.smart.campus.entity.vo.PaginationResultVO;


/**
 * 学生课时学习进度表 业务接口
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
	CourseStudyLessonProgress getCourseStudyLessonProgressById(Long id);


	/**
	 * 根据Id修改
	 */
	Integer updateCourseStudyLessonProgressById(CourseStudyLessonProgress bean,Long id);


	/**
	 * 根据Id删除
	 */
	Integer deleteCourseStudyLessonProgressById(Long id);


	/**
	 * 根据UserIdAndLessonId查询对象
	 */
	CourseStudyLessonProgress getCourseStudyLessonProgressByUserIdAndLessonId(Integer userId,String lessonId);


	/**
	 * 根据UserIdAndLessonId修改
	 */
	Integer updateCourseStudyLessonProgressByUserIdAndLessonId(CourseStudyLessonProgress bean,Integer userId,String lessonId);


	/**
	 * 根据UserIdAndLessonId删除
	 */
	Integer deleteCourseStudyLessonProgressByUserIdAndLessonId(Integer userId,String lessonId);

}