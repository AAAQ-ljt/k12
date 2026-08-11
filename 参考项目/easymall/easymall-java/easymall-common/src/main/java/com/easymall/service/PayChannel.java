package com.easymall.service;

import com.easymall.entity.dto.PayInfoDTO;
import com.easymall.entity.dto.PayOrderNotifyDTO;
import com.easymall.entity.enums.PayChannelEnum;

import java.math.BigDecimal;
import java.util.Map;

public interface PayChannel {

    /**
     * 获取支付信息
     *
     * @param payChannelEnum
     * @param payOrderId
     * @param subject
     * @param amount
     * @return
     */
    PayInfoDTO getPayUrl(PayChannelEnum payChannelEnum, String payOrderId, String subject, BigDecimal amount);

    /**
     * 校验支付回调
     *
     * @param requestParams
     * @param jsonBody
     * @return
     */
    PayOrderNotifyDTO payNotify(Map<String, String> requestParams, String jsonBody);


    /**
     * 查询订单
     *
     * @param payOrderId
     * @return
     */
    PayOrderNotifyDTO queryOrder(String payOrderId);

    /**
     * 退款
     *
     * @param payOrderId
     * @param refundAmount
     */
    void refund(String sourcePayOrderId, String payOrderId, BigDecimal refundAmount);

    /**
     * 关闭订单
     *
     * @param payOrderId
     */
    void closeOrder(String payOrderId);
}
