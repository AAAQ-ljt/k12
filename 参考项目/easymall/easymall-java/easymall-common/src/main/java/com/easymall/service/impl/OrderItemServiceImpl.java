package com.easymall.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.easymall.entity.enums.PageSize;
import com.easymall.entity.query.OrderItemQuery;
import com.easymall.entity.po.OrderItem;
import com.easymall.entity.vo.PaginationResultVO;
import com.easymall.entity.query.SimplePage;
import com.easymall.mappers.OrderItemMapper;
import com.easymall.service.OrderItemService;
import com.easymall.utils.StringTools;


/**
 * 订单明细表 业务接口实现
 */
@Service("orderItemService")
public class OrderItemServiceImpl implements OrderItemService {

	@Resource
	private OrderItemMapper<OrderItem, OrderItemQuery> orderItemMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<OrderItem> findListByParam(OrderItemQuery param) {
		return this.orderItemMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(OrderItemQuery param) {
		return this.orderItemMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<OrderItem> findListByPage(OrderItemQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<OrderItem> list = this.findListByParam(param);
		PaginationResultVO<OrderItem> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(OrderItem bean) {
		return this.orderItemMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<OrderItem> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.orderItemMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<OrderItem> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.orderItemMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(OrderItem bean, OrderItemQuery param) {
		StringTools.checkParam(param);
		return this.orderItemMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(OrderItemQuery param) {
		StringTools.checkParam(param);
		return this.orderItemMapper.deleteByParam(param);
	}

	/**
	 * 根据OrderItemId获取对象
	 */
	@Override
	public OrderItem getOrderItemByOrderItemId(String orderItemId) {
		return this.orderItemMapper.selectByOrderItemId(orderItemId);
	}

	/**
	 * 根据OrderItemId修改
	 */
	@Override
	public Integer updateOrderItemByOrderItemId(OrderItem bean, String orderItemId) {
		return this.orderItemMapper.updateByOrderItemId(bean, orderItemId);
	}

	/**
	 * 根据OrderItemId删除
	 */
	@Override
	public Integer deleteOrderItemByOrderItemId(String orderItemId) {
		return this.orderItemMapper.deleteByOrderItemId(orderItemId);
	}
}