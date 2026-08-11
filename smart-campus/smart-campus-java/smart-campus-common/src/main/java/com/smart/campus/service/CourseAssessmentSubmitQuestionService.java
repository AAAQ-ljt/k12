package com.smart.campus.service;

import java.util.List;

import com.smart.campus.entity.query.CourseAssessmentSubmitQuestionQuery;
import com.smart.campus.entity.po.CourseAssessmentSubmitQuestion;
import com.smart.campus.entity.vo.PaginationResultVO;


/**
 * 课程作业/考试学生答题明细表 业务接口
 */
public interface CourseAssessmentSubmitQuestionService {

	/**
	 * 根据条件查询列表
	 */
	List<CourseAssessmentSubmitQuestion> findListByParam(CourseAssessmentSubmitQuestionQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(CourseAssessmentSubmitQuestionQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<CourseAssessmentSubmitQuestion> findListByPage(CourseAssessmentSubmitQuestionQuery param);

	/**
	 * 新增
	 */
	Integer add(CourseAssessmentSubmitQuestion bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<CourseAssessmentSubmitQuestion> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<CourseAssessmentSubmitQuestion> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(CourseAssessmentSubmitQuestion bean,CourseAssessmentSubmitQuestionQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(CourseAssessmentSubmitQuestionQuery param);

	/**
	 * 根据Id查询对象
	 */
	CourseAssessmentSubmitQuestion getCourseAssessmentSubmitQuestionById(Long id);


	/**
	 * 根据Id修改
	 */
	Integer updateCourseAssessmentSubmitQuestionById(CourseAssessmentSubmitQuestion bean,Long id);


	/**
	 * 根据Id删除
	 */
	Integer deleteCourseAssessmentSubmitQuestionById(Long id);


	/**
	 * 根据SubmitIdAndQuestionId查询对象
	 */
	CourseAssessmentSubmitQuestion getCourseAssessmentSubmitQuestionBySubmitIdAndQuestionId(Long submitId,Integer questionId);


	/**
	 * 根据SubmitIdAndQuestionId修改
	 */
	Integer updateCourseAssessmentSubmitQuestionBySubmitIdAndQuestionId(CourseAssessmentSubmitQuestion bean,Long submitId,Integer questionId);


	/**
	 * 根据SubmitIdAndQuestionId删除
	 */
	Integer deleteCourseAssessmentSubmitQuestionBySubmitIdAndQuestionId(Long submitId,Integer questionId);

}