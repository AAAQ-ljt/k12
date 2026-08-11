package com.nexora.service;

import java.util.List;

import com.nexora.entity.query.KnowledgeDocQuery;
import com.nexora.entity.po.KnowledgeDoc;
import com.nexora.entity.vo.PaginationResultVO;


/**
 * 知识库文档表 业务接口
 */
public interface KnowledgeDocService {

	/**
	 * 根据条件查询列表
	 */
	List<KnowledgeDoc> findListByParam(KnowledgeDocQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(KnowledgeDocQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<KnowledgeDoc> findListByPage(KnowledgeDocQuery param);

	/**
	 * 新增
	 */
	Integer add(KnowledgeDoc bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<KnowledgeDoc> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<KnowledgeDoc> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(KnowledgeDoc bean,KnowledgeDocQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(KnowledgeDocQuery param);

	/**
	 * 根据DocId查询对象
	 */
	KnowledgeDoc getKnowledgeDocByDocId(String docId);


	/**
	 * 根据DocId修改
	 */
	Integer updateKnowledgeDocByDocId(KnowledgeDoc bean,String docId);


	/**
	 * 根据DocId删除
	 */
	Integer deleteKnowledgeDocByDocId(String docId);

}