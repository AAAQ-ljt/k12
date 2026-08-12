package com.easymall.mappers;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 数据库操作接口
 */
public interface ProductPropertyValueMapper<T, P> extends BaseMapper<T, P> {

    /**
     * 根据ProductIdAndPropertyValueId更新
     */
    Integer updateByProductIdAndPropertyValueId(@Param("bean") T t, @Param("productId") String productId, @Param("propertyValueId") String propertyValueId);


    /**
     * 根据ProductIdAndPropertyValueId删除
     */
    Integer deleteByProductIdAndPropertyValueId(@Param("productId") String productId, @Param("propertyValueId") String propertyValueId);


    /**
     * 根据ProductIdAndPropertyValueId获取对象
     */
    T selectByProductIdAndPropertyValueId(@Param("productId") String productId, @Param("propertyValueId") String propertyValueId);


    void deleteBatch(@Param("productId") String productId, @Param("dataList") List<T> dataList);

    void updateBatch(@Param("productId") String productId, @Param("dataList") List<T> dataList);

}
