package com.easymall.service.impl;

import com.easymall.entity.enums.PageSize;
import com.easymall.entity.po.ProductPropertyValue;
import com.easymall.entity.query.ProductPropertyValueQuery;
import com.easymall.entity.query.SimplePage;
import com.easymall.entity.vo.PaginationResultVO;
import com.easymall.mappers.ProductPropertyValueMapper;
import com.easymall.service.ProductPropertyValueService;
import com.easymall.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 *  业务接口实现
 */
@Service("productPropertyValueService")
public class ProductPropertyValueServiceImpl implements ProductPropertyValueService {

	@Resource
	private ProductPropertyValueMapper<ProductPropertyValue, ProductPropertyValueQuery> productPropertyValueMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<ProductPropertyValue> findListByParam(ProductPropertyValueQuery param) {
		return this.productPropertyValueMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(ProductPropertyValueQuery param) {
		return this.productPropertyValueMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<ProductPropertyValue> findListByPage(ProductPropertyValueQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<ProductPropertyValue> list = this.findListByParam(param);
		PaginationResultVO<ProductPropertyValue> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(ProductPropertyValue bean) {
		return this.productPropertyValueMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<ProductPropertyValue> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.productPropertyValueMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<ProductPropertyValue> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.productPropertyValueMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(ProductPropertyValue bean, ProductPropertyValueQuery param) {
		StringTools.checkParam(param);
		return this.productPropertyValueMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(ProductPropertyValueQuery param) {
		StringTools.checkParam(param);
		return this.productPropertyValueMapper.deleteByParam(param);
	}

	/**
	 * 根据ProductIdAndPropertyValueId获取对象
	 */
	@Override
	public ProductPropertyValue getProductPropertyValueByProductIdAndPropertyValueId(String productId, String propertyValueId) {
		return this.productPropertyValueMapper.selectByProductIdAndPropertyValueId(productId, propertyValueId);
	}

	/**
	 * 根据ProductIdAndPropertyValueId修改
	 */
	@Override
	public Integer updateProductPropertyValueByProductIdAndPropertyValueId(ProductPropertyValue bean, String productId, String propertyValueId) {
		return this.productPropertyValueMapper.updateByProductIdAndPropertyValueId(bean, productId, propertyValueId);
	}

	/**
	 * 根据ProductIdAndPropertyValueId删除
	 */
	@Override
	public Integer deleteProductPropertyValueByProductIdAndPropertyValueId(String productId, String propertyValueId) {
		return this.productPropertyValueMapper.deleteByProductIdAndPropertyValueId(productId, propertyValueId);
	}
}