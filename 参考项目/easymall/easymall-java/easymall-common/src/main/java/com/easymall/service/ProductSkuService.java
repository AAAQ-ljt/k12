package com.easymall.service;

import com.easymall.entity.po.ProductSku;
import com.easymall.entity.query.ProductSkuQuery;
import com.easymall.entity.vo.PaginationResultVO;
import com.easymall.entity.vo.ProductSkuVO;

import java.util.List;


/**
 * 业务接口
 */
public interface ProductSkuService {

    /**
     * 根据条件查询列表
     */
    List<ProductSku> findListByParam(ProductSkuQuery param);

    /**
     * 根据条件查询列表
     */
    Integer findCountByParam(ProductSkuQuery param);

    /**
     * 分页查询
     */
    PaginationResultVO<ProductSku> findListByPage(ProductSkuQuery param);

    /**
     * 新增
     */
    Integer add(ProductSku bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<ProductSku> listBean);

    /**
     * 批量新增/修改
     */
    Integer addOrUpdateBatch(List<ProductSku> listBean);

    /**
     * 多条件更新
     */
    Integer updateByParam(ProductSku bean, ProductSkuQuery param);

    /**
     * 多条件删除
     */
    Integer deleteByParam(ProductSkuQuery param);

    /**
     * 根据ProductIdAndPropertyValueIdHash查询对象
     */
    ProductSku getProductSkuByProductIdAndPropertyValueIdHash(String productId, String propertyValueIdHash);


    /**
     * 根据ProductIdAndPropertyValueIdHash修改
     */
    Integer updateProductSkuByProductIdAndPropertyValueIdHash(ProductSku bean, String productId, String propertyValueIdHash);


    /**
     * 根据ProductIdAndPropertyValueIdHash删除
     */
    Integer deleteProductSkuByProductIdAndPropertyValueIdHash(String productId, String propertyValueIdHash);

    void updateStock(String productId, String propertyValueIdHash, Integer changeStock);

    PaginationResultVO<ProductSkuVO> findListByPage4ListVO(ProductSkuQuery param);
}