package com.easymall.service;

import java.util.List;

import com.easymall.entity.query.ProductPropertyValueQuery;
import com.easymall.entity.po.ProductPropertyValue;
import com.easymall.entity.vo.PaginationResultVO;


/**
 *  业务接口
 */
public interface ProductPropertyValueService {

	/**
	 * 根据条件查询列表
	 */
	List<ProductPropertyValue> findListByParam(ProductPropertyValueQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(ProductPropertyValueQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<ProductPropertyValue> findListByPage(ProductPropertyValueQuery param);

	/**
	 * 新增
	 */
	Integer add(ProductPropertyValue bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<ProductPropertyValue> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<ProductPropertyValue> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(ProductPropertyValue bean,ProductPropertyValueQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(ProductPropertyValueQuery param);

	/**
	 * 根据ProductIdAndPropertyValueId查询对象
	 */
	ProductPropertyValue getProductPropertyValueByProductIdAndPropertyValueId(String productId,String propertyValueId);


	/**
	 * 根据ProductIdAndPropertyValueId修改
	 */
	Integer updateProductPropertyValueByProductIdAndPropertyValueId(ProductPropertyValue bean,String productId,String propertyValueId);


	/**
	 * 根据ProductIdAndPropertyValueId删除
	 */
	Integer deleteProductPropertyValueByProductIdAndPropertyValueId(String productId,String propertyValueId);

}