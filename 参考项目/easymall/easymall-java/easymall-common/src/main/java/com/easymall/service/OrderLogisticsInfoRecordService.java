package com.easymall.service;

import java.util.List;

import com.easymall.entity.query.OrderLogisticsInfoRecordQuery;
import com.easymall.entity.po.OrderLogisticsInfoRecord;
import com.easymall.entity.vo.PaginationResultVO;


/**
 *  业务接口
 */
public interface OrderLogisticsInfoRecordService {

	/**
	 * 根据条件查询列表
	 */
	List<OrderLogisticsInfoRecord> findListByParam(OrderLogisticsInfoRecordQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(OrderLogisticsInfoRecordQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<OrderLogisticsInfoRecord> findListByPage(OrderLogisticsInfoRecordQuery param);

	/**
	 * 新增
	 */
	Integer add(OrderLogisticsInfoRecord bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<OrderLogisticsInfoRecord> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<OrderLogisticsInfoRecord> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(OrderLogisticsInfoRecord bean,OrderLogisticsInfoRecordQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(OrderLogisticsInfoRecordQuery param);

	/**
	 * 根据RecordId查询对象
	 */
	OrderLogisticsInfoRecord getOrderLogisticsInfoRecordByRecordId(Integer recordId);


	/**
	 * 根据RecordId修改
	 */
	Integer updateOrderLogisticsInfoRecordByRecordId(OrderLogisticsInfoRecord bean,Integer recordId);


	/**
	 * 根据RecordId删除
	 */
	Integer deleteOrderLogisticsInfoRecordByRecordId(Integer recordId);

}