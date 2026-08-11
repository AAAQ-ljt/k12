package com.easymall.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 数据库操作接口
 */
public interface ProductInfoMapper<T, P> extends BaseMapper<T, P> {

    /**
     * 根据ProductId更新
     */
    Integer updateByProductId(@Param("bean") T t, @Param("productId") String productId);


    /**
     * 根据ProductId删除
     */
    Integer deleteByProductId(@Param("productId") String productId);


    /**
     * 根据ProductId获取对象
     */
    T selectByProductId(@Param("productId") String productId);

    void updateProdctuctTotalSale(@Param("productId") String productId, @Param("changeCount") Integer changeCount);

}
