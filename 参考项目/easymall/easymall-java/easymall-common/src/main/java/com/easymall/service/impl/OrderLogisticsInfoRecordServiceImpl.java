package com.easymall.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.easymall.entity.enums.PageSize;
import com.easymall.entity.query.OrderLogisticsInfoRecordQuery;
import com.easymall.entity.po.OrderLogisticsInfoRecord;
import com.easymall.entity.vo.PaginationResultVO;
import com.easymall.entity.query.SimplePage;
import com.easymall.mappers.OrderLogisticsInfoRecordMapper;
import com.easymall.service.OrderLogisticsInfoRecordService;
import com.easymall.utils.StringTools;


/**
 *  业务接口实现
 */
@Service("orderLogisticsInfoRecordService")
public class OrderLogisticsInfoRecordServiceImpl implements OrderLogisticsInfoRecordService {

	@Resource
	private OrderLogisticsInfoRecordMapper<OrderLogisticsInfoRecord, OrderLogisticsInfoRecordQuery> orderLogisticsInfoRecordMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<OrderLogisticsInfoRecord> findListByParam(OrderLogisticsInfoRecordQuery param) {
		return this.orderLogisticsInfoRecordMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(OrderLogisticsInfoRecordQuery param) {
		return this.orderLogisticsInfoRecordMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<OrderLogisticsInfoRecord> findListByPage(OrderLogisticsInfoRecordQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<OrderLogisticsInfoRecord> list = this.findListByParam(param);
		PaginationResultVO<OrderLogisticsInfoRecord> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(OrderLogisticsInfoRecord bean) {
		return this.orderLogisticsInfoRecordMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<OrderLogisticsInfoRecord> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.orderLogisticsInfoRecordMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<OrderLogisticsInfoRecord> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.orderLogisticsInfoRecordMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(OrderLogisticsInfoRecord bean, OrderLogisticsInfoRecordQuery param) {
		StringTools.checkParam(param);
		return this.orderLogisticsInfoRecordMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(OrderLogisticsInfoRecordQuery param) {
		StringTools.checkParam(param);
		return this.orderLogisticsInfoRecordMapper.deleteByParam(param);
	}

	/**
	 * 根据RecordId获取对象
	 */
	@Override
	public OrderLogisticsInfoRecord getOrderLogisticsInfoRecordByRecordId(Integer recordId) {
		return this.orderLogisticsInfoRecordMapper.selectByRecordId(recordId);
	}

	/**
	 * 根据RecordId修改
	 */
	@Override
	public Integer updateOrderLogisticsInfoRecordByRecordId(OrderLogisticsInfoRecord bean, Integer recordId) {
		return this.orderLogisticsInfoRecordMapper.updateByRecordId(bean, recordId);
	}

	/**
	 * 根据RecordId删除
	 */
	@Override
	public Integer deleteOrderLogisticsInfoRecordByRecordId(Integer recordId) {
		return this.orderLogisticsInfoRecordMapper.deleteByRecordId(recordId);
	}
}