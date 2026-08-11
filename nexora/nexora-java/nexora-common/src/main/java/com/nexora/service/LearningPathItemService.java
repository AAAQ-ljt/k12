package com.nexora.service;

import java.util.List;

import com.nexora.entity.query.LearningPathItemQuery;
import com.nexora.entity.po.LearningPathItem;
import com.nexora.entity.vo.PaginationResultVO;


/**
 * 路径节点表 业务接口
 */
public interface LearningPathItemService {

	/**
	 * 根据条件查询列表
	 */
	List<LearningPathItem> findListByParam(LearningPathItemQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(LearningPathItemQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<LearningPathItem> findListByPage(LearningPathItemQuery param);

	/**
	 * 新增
	 */
	Integer add(LearningPathItem bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<LearningPathItem> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<LearningPathItem> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(LearningPathItem bean,LearningPathItemQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(LearningPathItemQuery param);

	/**
	 * 根据ItemId查询对象
	 */
	LearningPathItem getLearningPathItemByItemId(String itemId);


	/**
	 * 根据ItemId修改
	 */
	Integer updateLearningPathItemByItemId(LearningPathItem bean,String itemId);


	/**
	 * 根据ItemId删除
	 */
	Integer deleteLearningPathItemByItemId(String itemId);

}