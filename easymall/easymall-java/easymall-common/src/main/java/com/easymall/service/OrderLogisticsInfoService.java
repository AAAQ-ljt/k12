package com.easymall.service;

import com.easymall.entity.po.OrderLogisticsInfo;
import com.easymall.entity.query.OrderLogisticsInfoQuery;
import com.easymall.entity.vo.PaginationResultVO;

import java.util.List;


/**
 * 物流信息表 业务接口
 */
public interface OrderLogisticsInfoService {

    /**
     * 根据条件查询列表
     */
    List<OrderLogisticsInfo> findListByParam(OrderLogisticsInfoQuery param);

    /**
     * 根据条件查询列表
     */
    Integer findCountByParam(OrderLogisticsInfoQuery param);

    /**
     * 分页查询
     */
    PaginationResultVO<OrderLogisticsInfo> findListByPage(OrderLogisticsInfoQuery param);

    /**
     * 新增
     */
    Integer add(OrderLogisticsInfo bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<OrderLogisticsInfo> listBean);

    /**
     * 批量新增/修改
     */
    Integer addOrUpdateBatch(List<OrderLogisticsInfo> listBean);

    /**
     * 多条件更新
     */
    Integer updateByParam(OrderLogisticsInfo bean, OrderLogisticsInfoQuery param);

    /**
     * 多条件删除
     */
    Integer deleteByParam(OrderLogisticsInfoQuery param);

    /**
     * 根据OrderId查询对象
     */
    OrderLogisticsInfo getOrderLogisticsInfoByOrderId(String orderId);


    /**
     * 根据OrderId修改
     */
    Integer updateOrderLogisticsInfoByOrderId(OrderLogisticsInfo bean, String orderId);


    /**
     * 根据OrderId删除
     */
    Integer deleteOrderLogisticsInfoByOrderId(String orderId);

    void delivery(OrderLogisticsInfo logisticsInfo);

    //模拟物流
    void mockOrderlogistics(String orderId);

    OrderLogisticsInfo getOrderLogisticsRecords(String userId, String orderId);
}