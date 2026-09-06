package com.nexora.service;

import java.util.List;

import com.nexora.entity.query.CourseLessonQuizQuery;
import com.nexora.entity.po.CourseLessonQuiz;
import com.nexora.entity.vo.PaginationResultVO;

/**
 * 课时通关测验配置表 业务接口
 */
public interface CourseLessonQuizService {

	/**
	 * 根据条件查询列表
	 */
	List<CourseLessonQuiz> findListByParam(CourseLessonQuizQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(CourseLessonQuizQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<CourseLessonQuiz> findListByPage(CourseLessonQuizQuery param);

	/**
	 * 新增
	 */
	Integer add(CourseLessonQuiz bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<CourseLessonQuiz> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<CourseLessonQuiz> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(CourseLessonQuiz bean, CourseLessonQuizQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(CourseLessonQuizQuery param);

	/**
	 * 根据LessonId查询对象
	 */
	CourseLessonQuiz getCourseLessonQuizByLessonId(String lessonId);

	/**
	 * 根据LessonId修改
	 */
	Integer updateCourseLessonQuizByLessonId(CourseLessonQuiz bean, String lessonId);

	/**
	 * 根据LessonId删除
	 */
	Integer deleteCourseLessonQuizByLessonId(String lessonId);
}