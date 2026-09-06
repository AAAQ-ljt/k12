package com.nexora.service;

import java.util.List;

import com.nexora.entity.po.CourseEnrollment;
import com.nexora.entity.query.CourseEnrollmentQuery;
import com.nexora.entity.vo.PaginationResultVO;


/**
 * 学生加入课程记录表 业务接口
 */
public interface CourseEnrollmentService {

	/**
	 * 根据条件查询列表
	 */
	List<CourseEnrollment> findListByParam(CourseEnrollmentQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(CourseEnrollmentQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<CourseEnrollment> findListByPage(CourseEnrollmentQuery param);

	/**
	 * 新增
	 */
	Integer add(CourseEnrollment bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<CourseEnrollment> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<CourseEnrollment> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(CourseEnrollment bean, CourseEnrollmentQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(CourseEnrollmentQuery param);

	/**
	 * 根据Id查询对象
	 */
	CourseEnrollment getCourseEnrollmentById(Long id);

	/**
	 * 根据Id修改
	 */
	Integer updateCourseEnrollmentById(CourseEnrollment bean, Long id);

	/**
	 * 根据Id删除
	 */
	Integer deleteCourseEnrollmentById(Long id);

	/**
	 * 根据UserIdAndCourseId查询对象
	 */
	CourseEnrollment getCourseEnrollmentByUserIdAndCourseId(String userId, String courseId);

	/**
	 * 根据UserIdAndCourseId修改
	 */
	Integer updateCourseEnrollmentByUserIdAndCourseId(CourseEnrollment bean, String userId, String courseId);

	/**
	 * 根据UserIdAndCourseId删除
	 */
	Integer deleteCourseEnrollmentByUserIdAndCourseId(String userId, String courseId);

}
