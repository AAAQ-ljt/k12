package com.easymall.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 订单明细表 数据库操作接口
 */
public interface OrderItemMapper<T, P> extends BaseMapper<T, P> {

    /**
     * 根据OrderItemId更新
     */
    Integer updateByOrderItemId(@Param("bean") T t, @Param("orderItemId") String orderItemId);


    /**
     * 根据OrderItemId删除
     */
    Integer deleteByOrderItemId(@Param("orderItemId") String orderItemId);


    /**
     * 根据OrderItemId获取对象
     */
    T selectByOrderItemId(@Param("orderItemId") String orderItemId);


}
