package com.smart.campus.service;

import java.util.List;

import com.smart.campus.entity.query.StudyPlanQuery;
import com.smart.campus.entity.po.StudyPlan;
import com.smart.campus.entity.vo.PaginationResultVO;


/**
 * 学生学习计划主表 业务接口
 */
public interface StudyPlanService {

	/**
	 * 根据条件查询列表
	 */
	List<StudyPlan> findListByParam(StudyPlanQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(StudyPlanQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<StudyPlan> findListByPage(StudyPlanQuery param);

	/**
	 * 新增
	 */
	Integer add(StudyPlan bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<StudyPlan> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<StudyPlan> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(StudyPlan bean,StudyPlanQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(StudyPlanQuery param);

	/**
	 * 根据PlanId查询对象
	 */
	StudyPlan getStudyPlanByPlanId(String planId);


	/**
	 * 根据PlanId修改
	 */
	Integer updateStudyPlanByPlanId(StudyPlan bean,String planId);


	/**
	 * 根据PlanId删除
	 */
	Integer deleteStudyPlanByPlanId(String planId);

}