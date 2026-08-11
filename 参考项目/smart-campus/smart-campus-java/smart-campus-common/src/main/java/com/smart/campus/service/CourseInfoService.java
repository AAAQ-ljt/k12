package com.smart.campus.service;

import java.util.List;

import com.smart.campus.entity.query.CourseInfoQuery;
import com.smart.campus.entity.po.CourseInfo;
import com.smart.campus.entity.vo.PaginationResultVO;


/**
 * 课程表 业务接口
 */
public interface CourseInfoService {

	/**
	 * 根据条件查询列表
	 */
	List<CourseInfo> findListByParam(CourseInfoQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(CourseInfoQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<CourseInfo> findListByPage(CourseInfoQuery param);

	/**
	 * 新增
	 */
	Integer add(CourseInfo bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<CourseInfo> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<CourseInfo> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(CourseInfo bean,CourseInfoQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(CourseInfoQuery param);

	/**
	 * 根据CourseId查询对象
	 */
	CourseInfo getCourseInfoByCourseId(String courseId);


	/**
	 * 根据CourseId修改
	 */
	Integer updateCourseInfoByCourseId(CourseInfo bean,String courseId);


	/**
	 * 根据CourseId删除
	 */
	Integer deleteCourseInfoByCourseId(String courseId);

}