package com.nexora.service;

import java.util.List;

import com.nexora.entity.query.KnowledgePointQuery;
import com.nexora.entity.po.KnowledgePoint;
import com.nexora.entity.vo.PaginationResultVO;


/**
 * 知识点表（领域中心） 业务接口
 */
public interface KnowledgePointService {

	/**
	 * 根据条件查询列表
	 */
	List<KnowledgePoint> findListByParam(KnowledgePointQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(KnowledgePointQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<KnowledgePoint> findListByPage(KnowledgePointQuery param);

	/**
	 * 新增
	 */
	Integer add(KnowledgePoint bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<KnowledgePoint> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<KnowledgePoint> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(KnowledgePoint bean,KnowledgePointQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(KnowledgePointQuery param);

	/**
	 * 根据KnowledgePointId查询对象
	 */
	KnowledgePoint getKnowledgePointByKnowledgePointId(String knowledgePointId);


	/**
	 * 根据KnowledgePointId修改
	 */
	Integer updateKnowledgePointByKnowledgePointId(KnowledgePoint bean,String knowledgePointId);


	/**
	 * 根据KnowledgePointId删除
	 */
	Integer deleteKnowledgePointByKnowledgePointId(String knowledgePointId);

}