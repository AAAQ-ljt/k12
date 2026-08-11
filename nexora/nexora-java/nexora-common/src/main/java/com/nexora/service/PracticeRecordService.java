package com.nexora.service;

import java.util.List;

import com.nexora.entity.query.PracticeRecordQuery;
import com.nexora.entity.po.PracticeRecord;
import com.nexora.entity.vo.PaginationResultVO;


/**
 * 游戏化练习记录表 业务接口
 */
public interface PracticeRecordService {

	/**
	 * 根据条件查询列表
	 */
	List<PracticeRecord> findListByParam(PracticeRecordQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(PracticeRecordQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<PracticeRecord> findListByPage(PracticeRecordQuery param);

	/**
	 * 新增
	 */
	Integer add(PracticeRecord bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<PracticeRecord> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<PracticeRecord> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(PracticeRecord bean,PracticeRecordQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(PracticeRecordQuery param);

	/**
	 * 根据RecordId查询对象
	 */
	PracticeRecord getPracticeRecordByRecordId(Long recordId);


	/**
	 * 根据RecordId修改
	 */
	Integer updatePracticeRecordByRecordId(PracticeRecord bean,Long recordId);


	/**
	 * 根据RecordId删除
	 */
	Integer deletePracticeRecordByRecordId(Long recordId);

}