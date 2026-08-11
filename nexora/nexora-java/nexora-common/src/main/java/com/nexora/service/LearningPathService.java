package com.nexora.service;

import java.util.List;

import com.nexora.entity.query.LearningPathQuery;
import com.nexora.entity.po.LearningPath;
import com.nexora.entity.vo.PaginationResultVO;


/**
 * 学习路径表 业务接口
 */
public interface LearningPathService {

	/**
	 * 根据条件查询列表
	 */
	List<LearningPath> findListByParam(LearningPathQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(LearningPathQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<LearningPath> findListByPage(LearningPathQuery param);

	/**
	 * 新增
	 */
	Integer add(LearningPath bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<LearningPath> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<LearningPath> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(LearningPath bean,LearningPathQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(LearningPathQuery param);

	/**
	 * 根据PathId查询对象
	 */
	LearningPath getLearningPathByPathId(String pathId);


	/**
	 * 根据PathId修改
	 */
	Integer updateLearningPathByPathId(LearningPath bean,String pathId);


	/**
	 * 根据PathId删除
	 */
	Integer deleteLearningPathByPathId(String pathId);

}