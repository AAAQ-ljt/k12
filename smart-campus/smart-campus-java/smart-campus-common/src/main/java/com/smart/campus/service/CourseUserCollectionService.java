package com.smart.campus.service;

import java.util.List;

import com.smart.campus.entity.query.CourseUserCollectionQuery;
import com.smart.campus.entity.po.CourseUserCollection;
import com.smart.campus.entity.vo.PaginationResultVO;


/**
 * 课程收藏 业务接口
 */
public interface CourseUserCollectionService {

	/**
	 * 根据条件查询列表
	 */
	List<CourseUserCollection> findListByParam(CourseUserCollectionQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(CourseUserCollectionQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<CourseUserCollection> findListByPage(CourseUserCollectionQuery param);

	/**
	 * 新增
	 */
	Integer add(CourseUserCollection bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<CourseUserCollection> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<CourseUserCollection> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(CourseUserCollection bean,CourseUserCollectionQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(CourseUserCollectionQuery param);

	/**
	 * 根据CourseIdAndUserId查询对象
	 */
	CourseUserCollection getCourseUserCollectionByCourseIdAndUserId(String courseId,Integer userId);


	/**
	 * 根据CourseIdAndUserId修改
	 */
	Integer updateCourseUserCollectionByCourseIdAndUserId(CourseUserCollection bean,String courseId,Integer userId);


	/**
	 * 根据CourseIdAndUserId删除
	 */
	Integer deleteCourseUserCollectionByCourseIdAndUserId(String courseId,Integer userId);

}