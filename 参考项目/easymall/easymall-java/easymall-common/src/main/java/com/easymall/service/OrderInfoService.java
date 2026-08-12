package com.easymall.service;

import com.easymall.entity.dto.PayInfoDTO;
import com.easymall.entity.dto.PayOrderNotifyDTO;
import com.easymall.entity.dto.PostOrderDTO;
import com.easymall.entity.enums.OrderStatusEnum;
import com.easymall.entity.enums.PayChannelEnum;
import com.easymall.entity.po.OrderInfo;
import com.easymall.entity.query.OrderInfoQuery;
import com.easymall.entity.vo.PaginationResultVO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


/**
 * 订单信息 业务接口
 */
public interface OrderInfoService {

    /**
     * 根据条件查询列表
     */
    List<OrderInfo> findListByParam(OrderInfoQuery param);

    /**
     * 根据条件查询列表
     */
    Integer findCountByParam(OrderInfoQuery param);

    /**
     * 分页查询
     */
    PaginationResultVO<OrderInfo> findListByPage(OrderInfoQuery param);

    /**
     * 新增
     */
    Integer add(OrderInfo bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<OrderInfo> listBean);

    /**
     * 批量新增/修改
     */
    Integer addOrUpdateBatch(List<OrderInfo> listBean);

    /**
     * 多条件更新
     */
    Integer updateByParam(OrderInfo bean, OrderInfoQuery param);

    /**
     * 多条件删除
     */
    Integer deleteByParam(OrderInfoQuery param);

    /**
     * 根据OrderId查询对象
     */
    OrderInfo getOrderInfoByOrderId(String orderId);


    /**
     * 根据OrderId修改
     */
    Integer updateOrderInfoByOrderId(OrderInfo bean, String orderId);


    /**
     * 根据OrderId删除
     */
    Integer deleteOrderInfoByOrderId(String orderId);

    PayInfoDTO postOrder(String userId, PostOrderDTO postOrderDTO);

    void payNotify(PayChannelEnum payChannelEnum, Map<String, String> requestParams, String jsonBody);

    void payOrderSuccess(PayOrderNotifyDTO payOrderNotifyDTO);

    PayInfoDTO getPayInfo(String userId, String orderId);

    void cancelOrder(String userId, String orderId, OrderStatusEnum orderStatusEnum);

    void deleteOrder(String userId, String orderId);

    void confirmOrder(String userId, String orderId);

    void refundByOrderItemId(String userId, String orderItemId);

    void refundByOrderId(String userId, String orderId);

    BigDecimal getOrderTotalAmount(String orderTime, Integer[] orderStatus);
}