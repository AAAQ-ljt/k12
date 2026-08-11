package com.nexora.service;

import java.util.List;

import com.nexora.entity.query.CourseStudyLogQuery;
import com.nexora.entity.po.CourseStudyLog;
import com.nexora.entity.vo.PaginationResultVO;


/**
 * 学习日志表 业务接口
 */
public interface CourseStudyLogService {

	/**
	 * 根据条件查询列表
	 */
	List<CourseStudyLog> findListByParam(CourseStudyLogQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(CourseStudyLogQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<CourseStudyLog> findListByPage(CourseStudyLogQuery param);

	/**
	 * 新增
	 */
	Integer add(CourseStudyLog bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<CourseStudyLog> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<CourseStudyLog> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(CourseStudyLog bean,CourseStudyLogQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(CourseStudyLogQuery param);

	/**
	 * 根据Id查询对象
	 */
	CourseStudyLog getCourseStudyLogById(Long id);


	/**
	 * 根据Id修改
	 */
	Integer updateCourseStudyLogById(CourseStudyLog bean,Long id);


	/**
	 * 根据Id删除
	 */
	Integer deleteCourseStudyLogById(Long id);

}