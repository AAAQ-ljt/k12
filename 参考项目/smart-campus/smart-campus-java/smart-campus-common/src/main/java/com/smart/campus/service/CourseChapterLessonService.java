package com.smart.campus.service;

import java.util.List;

import com.smart.campus.entity.query.CourseChapterLessonQuery;
import com.smart.campus.entity.po.CourseChapterLesson;
import com.smart.campus.entity.vo.PaginationResultVO;


/**
 * 课程课时表 业务接口
 */
public interface CourseChapterLessonService {

	/**
	 * 根据条件查询列表
	 */
	List<CourseChapterLesson> findListByParam(CourseChapterLessonQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(CourseChapterLessonQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<CourseChapterLesson> findListByPage(CourseChapterLessonQuery param);

	/**
	 * 新增
	 */
	Integer add(CourseChapterLesson bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<CourseChapterLesson> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<CourseChapterLesson> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(CourseChapterLesson bean,CourseChapterLessonQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(CourseChapterLessonQuery param);

	/**
	 * 根据LessonId查询对象
	 */
	CourseChapterLesson getCourseChapterLessonByLessonId(String lessonId);


	/**
	 * 根据LessonId修改
	 */
	Integer updateCourseChapterLessonByLessonId(CourseChapterLesson bean,String lessonId);


	/**
	 * 根据LessonId删除
	 */
	Integer deleteCourseChapterLessonByLessonId(String lessonId);

}