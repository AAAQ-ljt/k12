package com.nexora.service;

import java.util.List;

import com.nexora.entity.query.KnowledgeMasteryQuery;
import com.nexora.entity.po.KnowledgeMastery;
import com.nexora.entity.vo.PaginationResultVO;


/**
 * 知识点掌握度表 业务接口
 */
public interface KnowledgeMasteryService {

	/**
	 * 根据条件查询列表
	 */
	List<KnowledgeMastery> findListByParam(KnowledgeMasteryQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(KnowledgeMasteryQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<KnowledgeMastery> findListByPage(KnowledgeMasteryQuery param);

	/**
	 * 新增
	 */
	Integer add(KnowledgeMastery bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<KnowledgeMastery> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<KnowledgeMastery> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(KnowledgeMastery bean,KnowledgeMasteryQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(KnowledgeMasteryQuery param);

	/**
	 * 根据Id查询对象
	 */
	KnowledgeMastery getKnowledgeMasteryById(Integer id);


	/**
	 * 根据Id修改
	 */
	Integer updateKnowledgeMasteryById(KnowledgeMastery bean,Integer id);


	/**
	 * 根据Id删除
	 */
	Integer deleteKnowledgeMasteryById(Integer id);


	/**
	 * 根据UserIdAndKnowledgePointId查询对象
	 */
	KnowledgeMastery getKnowledgeMasteryByUserIdAndKnowledgePointId(String userId,String knowledgePointId);


	/**
	 * 根据UserIdAndKnowledgePointId修改
	 */
	Integer updateKnowledgeMasteryByUserIdAndKnowledgePointId(KnowledgeMastery bean,String userId,String knowledgePointId);


	/**
	 * 根据UserIdAndKnowledgePointId删除
	 */
	Integer deleteKnowledgeMasteryByUserIdAndKnowledgePointId(String userId,String knowledgePointId);

}