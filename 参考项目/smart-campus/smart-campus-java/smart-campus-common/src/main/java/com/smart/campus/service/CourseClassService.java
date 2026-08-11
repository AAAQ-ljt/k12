package com.smart.campus.service;

import java.util.List;

import com.smart.campus.entity.query.CourseClassQuery;
import com.smart.campus.entity.po.CourseClass;
import com.smart.campus.entity.vo.PaginationResultVO;


/**
 * 课程班级关联表 业务接口
 */
public interface CourseClassService {

	/**
	 * 根据条件查询列表
	 */
	List<CourseClass> findListByParam(CourseClassQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(CourseClassQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<CourseClass> findListByPage(CourseClassQuery param);

	/**
	 * 新增
	 */
	Integer add(CourseClass bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<CourseClass> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<CourseClass> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(CourseClass bean,CourseClassQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(CourseClassQuery param);

	/**
	 * 根据CourseIdAndClassId查询对象
	 */
	CourseClass getCourseClassByCourseIdAndClassId(String courseId,Integer classId);


	/**
	 * 根据CourseIdAndClassId修改
	 */
	Integer updateCourseClassByCourseIdAndClassId(CourseClass bean,String courseId,Integer classId);


	/**
	 * 根据CourseIdAndClassId删除
	 */
	Integer deleteCourseClassByCourseIdAndClassId(String courseId,Integer classId);

}