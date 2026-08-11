package com.smart.campus.service;

import java.util.List;

import com.smart.campus.entity.query.CourseAssessmentSubmitQuery;
import com.smart.campus.entity.po.CourseAssessmentSubmit;
import com.smart.campus.entity.vo.PaginationResultVO;


/**
 * 课程作业/考试学生提交表 业务接口
 */
public interface CourseAssessmentSubmitService {

	/**
	 * 根据条件查询列表
	 */
	List<CourseAssessmentSubmit> findListByParam(CourseAssessmentSubmitQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(CourseAssessmentSubmitQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<CourseAssessmentSubmit> findListByPage(CourseAssessmentSubmitQuery param);

	/**
	 * 新增
	 */
	Integer add(CourseAssessmentSubmit bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<CourseAssessmentSubmit> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<CourseAssessmentSubmit> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(CourseAssessmentSubmit bean,CourseAssessmentSubmitQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(CourseAssessmentSubmitQuery param);

	/**
	 * 根据SubmitId查询对象
	 */
	CourseAssessmentSubmit getCourseAssessmentSubmitBySubmitId(Long submitId);


	/**
	 * 根据SubmitId修改
	 */
	Integer updateCourseAssessmentSubmitBySubmitId(CourseAssessmentSubmit bean,Long submitId);


	/**
	 * 根据SubmitId删除
	 */
	Integer deleteCourseAssessmentSubmitBySubmitId(Long submitId);


	/**
	 * 根据TaskIdAndUserIdAndPaperId查询对象
	 */
	CourseAssessmentSubmit getCourseAssessmentSubmitByTaskIdAndUserIdAndPaperId(String taskId,Integer userId,String paperId);


	/**
	 * 根据TaskIdAndUserIdAndPaperId修改
	 */
	Integer updateCourseAssessmentSubmitByTaskIdAndUserIdAndPaperId(CourseAssessmentSubmit bean,String taskId,Integer userId,String paperId);


	/**
	 * 根据TaskIdAndUserIdAndPaperId删除
	 */
	Integer deleteCourseAssessmentSubmitByTaskIdAndUserIdAndPaperId(String taskId,Integer userId,String paperId);

}