package com.easymall.service;

import com.easymall.entity.dto.ProductSaveDTO;
import com.easymall.entity.po.ProductInfo;
import com.easymall.entity.query.ProductInfoQuery;
import com.easymall.entity.vo.PaginationResultVO;
import com.easymall.entity.vo.ProductInfoDetailVO;
import com.easymall.entity.vo.ProductListVO;

import java.util.List;


/**
 * 业务接口
 */
public interface ProductInfoService {

    /**
     * 根据条件查询列表
     */
    List<ProductInfo> findListByParam(ProductInfoQuery param);

    /**
     * 根据条件查询列表
     */
    Integer findCountByParam(ProductInfoQuery param);

    /**
     * 分页查询
     */
    PaginationResultVO<ProductInfo> findListByPage(ProductInfoQuery param);

    /**
     * 新增
     */
    Integer add(ProductInfo bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<ProductInfo> listBean);

    /**
     * 批量新增/修改
     */
    Integer addOrUpdateBatch(List<ProductInfo> listBean);

    /**
     * 多条件更新
     */
    Integer updateByParam(ProductInfo bean, ProductInfoQuery param);

    /**
     * 多条件删除
     */
    Integer deleteByParam(ProductInfoQuery param);

    /**
     * 根据ProductId查询对象
     */
    ProductInfo getProductInfoByProductId(String productId);


    /**
     * 根据ProductId修改
     */
    Integer updateProductInfoByProductId(ProductInfo bean, String productId);


    /**
     * 根据ProductId删除
     */
    Integer deleteProductInfoByProductId(String productId);

    void saveProduct(ProductSaveDTO productSaveDTO);

    ProductInfoDetailVO getProductInfo(String productId);

    PaginationResultVO<ProductListVO> findListByPage4ListVO(ProductInfoQuery param);

    void updateStatus(String productId, Integer status);

    void deleteProduct(String productId);
}