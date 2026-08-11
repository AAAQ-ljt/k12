package com.easymall.mappers;

import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单信息 数据库操作接口
 */
public interface OrderInfoMapper<T, P> extends BaseMapper<T, P> {

    /**
     * 根据OrderId更新
     */
    Integer updateByOrderId(@Param("bean") T t, @Param("orderId") String orderId);


    /**
     * 根据OrderId删除
     */
    Integer deleteByOrderId(@Param("orderId") String orderId);


    /**
     * 根据OrderId获取对象
     */
    T selectByOrderId(@Param("orderId") String orderId);


    BigDecimal selectOrderTotalAmount(@Param("orderTime") String orderTime, @Param("orderStatus") Integer[] orderStatus);

    Integer updateOrderStatusBatch(@Param("orderStatus") Integer orderStatus, @Param("oldStatus") Integer oldStatus,
                                   @Param("orderIdList") List<String> orderIdList);

}
