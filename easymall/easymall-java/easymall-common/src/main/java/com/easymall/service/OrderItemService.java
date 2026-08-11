package com.easymall.service;

import java.util.List;

import com.easymall.entity.query.OrderItemQuery;
import com.easymall.entity.po.OrderItem;
import com.easymall.entity.vo.PaginationResultVO;


/**
 * 订单明细表 业务接口
 */
public interface OrderItemService {

	/**
	 * 根据条件查询列表
	 */
	List<OrderItem> findListByParam(OrderItemQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(OrderItemQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<OrderItem> findListByPage(OrderItemQuery param);

	/**
	 * 新增
	 */
	Integer add(OrderItem bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<OrderItem> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<OrderItem> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(OrderItem bean,OrderItemQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(OrderItemQuery param);

	/**
	 * 根据OrderItemId查询对象
	 */
	OrderItem getOrderItemByOrderItemId(String orderItemId);


	/**
	 * 根据OrderItemId修改
	 */
	Integer updateOrderItemByOrderItemId(OrderItem bean,String orderItemId);


	/**
	 * 根据OrderItemId删除
	 */
	Integer deleteOrderItemByOrderItemId(String orderItemId);

}