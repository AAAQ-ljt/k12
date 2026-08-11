package com.smart.campus.service;

import java.util.List;

import com.smart.campus.entity.query.CourseStudyProgressQuery;
import com.smart.campus.entity.po.CourseStudyProgress;
import com.smart.campus.entity.vo.PaginationResultVO;


/**
 * 学生课程学习进度表 业务接口
 */
public interface CourseStudyProgressService {

	/**
	 * 根据条件查询列表
	 */
	List<CourseStudyProgress> findListByParam(CourseStudyProgressQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(CourseStudyProgressQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<CourseStudyProgress> findListByPage(CourseStudyProgressQuery param);

	/**
	 * 新增
	 */
	Integer add(CourseStudyProgress bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<CourseStudyProgress> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<CourseStudyProgress> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(CourseStudyProgress bean,CourseStudyProgressQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(CourseStudyProgressQuery param);

	/**
	 * 根据Id查询对象
	 */
	CourseStudyProgress getCourseStudyProgressById(Long id);


	/**
	 * 根据Id修改
	 */
	Integer updateCourseStudyProgressById(CourseStudyProgress bean,Long id);


	/**
	 * 根据Id删除
	 */
	Integer deleteCourseStudyProgressById(Long id);


	/**
	 * 根据UserIdAndCourseId查询对象
	 */
	CourseStudyProgress getCourseStudyProgressByUserIdAndCourseId(Integer userId,String courseId);


	/**
	 * 根据UserIdAndCourseId修改
	 */
	Integer updateCourseStudyProgressByUserIdAndCourseId(CourseStudyProgress bean,Integer userId,String courseId);


	/**
	 * 根据UserIdAndCourseId删除
	 */
	Integer deleteCourseStudyProgressByUserIdAndCourseId(Integer userId,String courseId);

}