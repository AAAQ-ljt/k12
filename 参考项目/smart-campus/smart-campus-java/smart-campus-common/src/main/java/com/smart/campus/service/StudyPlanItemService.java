package com.smart.campus.service;

import java.util.List;

import com.smart.campus.entity.query.StudyPlanItemQuery;
import com.smart.campus.entity.po.StudyPlanItem;
import com.smart.campus.entity.vo.PaginationResultVO;


/**
 * 学生学习计划明细表 业务接口
 */
public interface StudyPlanItemService {

	/**
	 * 根据条件查询列表
	 */
	List<StudyPlanItem> findListByParam(StudyPlanItemQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(StudyPlanItemQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<StudyPlanItem> findListByPage(StudyPlanItemQuery param);

	/**
	 * 新增
	 */
	Integer add(StudyPlanItem bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<StudyPlanItem> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<StudyPlanItem> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(StudyPlanItem bean,StudyPlanItemQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(StudyPlanItemQuery param);

	/**
	 * 根据ItemId查询对象
	 */
	StudyPlanItem getStudyPlanItemByItemId(Long itemId);


	/**
	 * 根据ItemId修改
	 */
	Integer updateStudyPlanItemByItemId(StudyPlanItem bean,Long itemId);


	/**
	 * 根据ItemId删除
	 */
	Integer deleteStudyPlanItemByItemId(Long itemId);

}