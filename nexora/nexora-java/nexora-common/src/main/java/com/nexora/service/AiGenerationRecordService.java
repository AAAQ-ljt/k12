package com.nexora.service;

import java.util.List;

import com.nexora.entity.query.AiGenerationRecordQuery;
import com.nexora.entity.po.AiGenerationRecord;
import com.nexora.entity.vo.PaginationResultVO;


/**
 * AI生成记录表 业务接口
 */
public interface AiGenerationRecordService {

	/**
	 * 根据条件查询列表
	 */
	List<AiGenerationRecord> findListByParam(AiGenerationRecordQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(AiGenerationRecordQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<AiGenerationRecord> findListByPage(AiGenerationRecordQuery param);

	/**
	 * 新增
	 */
	Integer add(AiGenerationRecord bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<AiGenerationRecord> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<AiGenerationRecord> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(AiGenerationRecord bean,AiGenerationRecordQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(AiGenerationRecordQuery param);

	/**
	 * 根据RecordId查询对象
	 */
	AiGenerationRecord getAiGenerationRecordByRecordId(String recordId);


	/**
	 * 根据RecordId修改
	 */
	Integer updateAiGenerationRecordByRecordId(AiGenerationRecord bean,String recordId);


	/**
	 * 根据RecordId删除
	 */
	Integer deleteAiGenerationRecordByRecordId(String recordId);

}