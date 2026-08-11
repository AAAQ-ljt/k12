package com.smart.campus.service;

import java.util.List;

import com.smart.campus.entity.query.CourseStudyLogQuery;
import com.smart.campus.entity.po.CourseStudyLog;
import com.smart.campus.entity.vo.PaginationResultVO;


/**
 * 学生学习流水表 业务接口
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


	/**
	 * 根据SessionId查询对象
	 */
	CourseStudyLog getCourseStudyLogBySessionId(String sessionId);


	/**
	 * 根据SessionId修改
	 */
	Integer updateCourseStudyLogBySessionId(CourseStudyLog bean,String sessionId);


	/**
	 * 根据SessionId删除
	 */
	Integer deleteCourseStudyLogBySessionId(String sessionId);

}