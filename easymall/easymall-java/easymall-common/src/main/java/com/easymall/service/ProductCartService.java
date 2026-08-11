package com.easymall.service;

import com.easymall.entity.po.ProductCart;
import com.easymall.entity.query.ProductCartQuery;
import com.easymall.entity.vo.PaginationResultVO;
import com.easymall.entity.vo.ProductSkuVO;

import java.util.List;


/**
 * 购物车 业务接口
 */
public interface ProductCartService {

    /**
     * 根据条件查询列表
     */
    List<ProductCart> findListByParam(ProductCartQuery param);

    /**
     * 根据条件查询列表
     */
    Integer findCountByParam(ProductCartQuery param);

    /**
     * 分页查询
     */
    PaginationResultVO<ProductCart> findListByPage(ProductCartQuery param);

    /**
     * 新增
     */
    Integer add(ProductCart bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<ProductCart> listBean);

    /**
     * 批量新增/修改
     */
    Integer addOrUpdateBatch(List<ProductCart> listBean);

    /**
     * 多条件更新
     */
    Integer updateByParam(ProductCart bean, ProductCartQuery param);

    /**
     * 多条件删除
     */
    Integer deleteByParam(ProductCartQuery param);

    /**
     * 根据CartId查询对象
     */
    ProductCart getProductCartByCartId(String cartId);


    /**
     * 根据CartId修改
     */
    Integer updateProductCartByCartId(ProductCart bean, String cartId);


    /**
     * 根据CartId删除
     */
    Integer deleteProductCartByCartId(String cartId);


    /**
     * 根据ProductIdAndPropertyValueIdHashAndUserId查询对象
     */
    ProductCart getProductCartByProductIdAndPropertyValueIdHashAndUserId(String productId, String propertyValueIdHash, String userId);


    /**
     * 根据ProductIdAndPropertyValueIdHashAndUserId修改
     */
    Integer updateProductCartByProductIdAndPropertyValueIdHashAndUserId(ProductCart bean, String productId, String propertyValueIdHash, String userId);


    /**
     * 根据ProductIdAndPropertyValueIdHashAndUserId删除
     */
    Integer deleteProductCartByProductIdAndPropertyValueIdHashAndUserId(String productId, String propertyValueIdHash, String userId);

    void add2Cart(ProductCart cart);

    PaginationResultVO<ProductSkuVO> loadProductCart(ProductCartQuery query);
}