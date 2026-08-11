package com.easymall.task;

import com.easymall.component.RedisComponent;
import com.easymall.component.SpringContext;
import com.easymall.constants.Constants;
import com.easymall.entity.config.AppConfig;
import com.easymall.entity.dto.PayOrderNotifyDTO;
import com.easymall.entity.enums.ExecutorServiceSingletonEnum;
import com.easymall.entity.enums.OrderStatusEnum;
import com.easymall.entity.enums.PayChannelEnum;
import com.easymall.entity.po.OrderInfo;
import com.easymall.entity.po.OrderLogisticsInfo;
import com.easymall.entity.query.OrderInfoQuery;
import com.easymall.service.OrderInfoService;
import com.easymall.service.OrderLogisticsInfoService;
import com.easymall.service.PayChannel;
import com.easymall.utils.StringTools;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class PayOrderTask {


    @Resource
    private AppConfig appConfig;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private OrderInfoService orderInfoService;

    @Resource
    private OrderLogisticsInfoService orderLogisticsInfoService;

    /**
     * 实际项目会使用 回调通知，不会采用轮训校验支付状态，回调通知必须将服务发布，否则无法收到回调通知
     */
    @PostConstruct
    public void checkPayOrder() {
        if (!appConfig.getAutoCheckpay()) {
            return;
        }
        ExecutorServiceSingletonEnum.INSTANCE.getExecutorService().execute(() -> {
            while (true) {
                try {
                    OrderInfoQuery orderInfoQuery = new OrderInfoQuery();
                    orderInfoQuery.setOrderStatus(OrderStatusEnum.WAIT_PAYMENT.getStatus());
                    List<OrderInfo> orderInfoList = orderInfoService.findListByParam(orderInfoQuery);
                    for (OrderInfo orderInfo : orderInfoList) {
                        PayChannelEnum payChannelEnum = PayChannelEnum.getByPayScene(orderInfo.getPayScene());
                        PayChannel payChannel = (PayChannel) SpringContext.getBean(payChannelEnum.getBeanName());
                        PayOrderNotifyDTO payOrderNotifyDTO = payChannel.queryOrder(orderInfo.getPayOrderId());
                        if (payOrderNotifyDTO == null) {
                            continue;
                        }
                        orderInfoService.payOrderSuccess(payOrderNotifyDTO);
                    }
                    Thread.sleep(10000);
                } catch (Exception e) {
                    log.error("查询支付订单失败", e);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
    }

    /**
     * 超时订单
     */
    @PostConstruct
    public void consumeDelayOrder() {
        ExecutorServiceSingletonEnum.INSTANCE.getExecutorService().execute(() -> {
            while (true) {
                try {
                    Set<String> queueOrderList = redisComponent.getTimeOutOrder(Constants.REDIS_KEY_ORDER_DELAY_QUEUE);
                    if (queueOrderList == null || queueOrderList.isEmpty()) {
                        Thread.sleep(5000);
                        continue;
                    }
                    for (String orderId : queueOrderList) {
                        if (redisComponent.removeTimeOutOrder(Constants.REDIS_KEY_ORDER_DELAY_QUEUE, orderId) > 0) {
                            OrderInfo orderInfo = orderInfoService.getOrderInfoByOrderId(orderId);
                            if (!OrderStatusEnum.WAIT_PAYMENT.getStatus().equals(orderInfo.getOrderStatus())) {
                                continue;
                            }
                            orderInfoService.cancelOrder(null, orderId, OrderStatusEnum.CLOSED);
                        }
                    }
                } catch (Exception e) {
                    log.error("处理超时订单失败", e);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
    }

    /**
     * 系统自动发货
     */
    @PostConstruct
    public void consumeDeliveryOrder() {
        ExecutorServiceSingletonEnum.INSTANCE.getExecutorService().execute(() -> {
            while (true) {
                try {
                    Set<String> queuePayOrderList = redisComponent.getTimeOutOrder(Constants.REDIS_KEY_ORDER_DELAY_QUEUE_DELIVERY);
                    if (queuePayOrderList == null || queuePayOrderList.isEmpty()) {
                        Thread.sleep(5000);
                        continue;
                    }
                    for (String payOrderId : queuePayOrderList) {
                        if (redisComponent.removeTimeOutOrder(Constants.REDIS_KEY_ORDER_DELAY_QUEUE_DELIVERY, payOrderId) > 0) {
                            OrderInfoQuery orderInfoQuery = new OrderInfoQuery();
                            orderInfoQuery.setPayOrderId(payOrderId);
                            List<OrderInfo> orderInfoList = orderInfoService.findListByParam(orderInfoQuery);
                            for (OrderInfo orderInfo : orderInfoList) {
                                try {
                                    OrderLogisticsInfo logisticsInfo = new OrderLogisticsInfo();
                                    logisticsInfo.setLogisticsNo("SF" + StringTools.getRandomNumber(10));
                                    logisticsInfo.setLogisticsCompany("顺丰");
                                    logisticsInfo.setOrderId(orderInfo.getOrderId());
                                    orderLogisticsInfoService.delivery(logisticsInfo);
                                } catch (Exception e) {
                                    log.error("自动发货失败", e);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("处理超时订单失败", e);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
    }

    /**
     * 系统自动确认收货
     */
    @PostConstruct
    public void consumeConfirmOrder() {
        ExecutorServiceSingletonEnum.INSTANCE.getExecutorService().execute(() -> {
            while (true) {
                try {
                        Set<String> queuePayOrderList = redisComponent.getTimeOutOrder(Constants.REDIS_KEY_ORDER_DELAY_QUEUE_CONFIRM);
                    if (queuePayOrderList == null || queuePayOrderList.isEmpty()) {
                        Thread.sleep(5000);
                        continue;
                    }
                    for (String orderId : queuePayOrderList) {
                        if (redisComponent.removeTimeOutOrder(Constants.REDIS_KEY_ORDER_DELAY_QUEUE_CONFIRM, orderId) > 0) {
                            try {
                                orderInfoService.confirmOrder(null, orderId);
                            } catch (Exception e) {
                                log.error("自动确认收货失败", e);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("自动确认收货失败", e);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
    }

    @PostConstruct
    public void consumeLogistics() {
        ExecutorServiceSingletonEnum.INSTANCE.getExecutorService().execute(() -> {
            while (true) {
                try {
                    Set<String> queueOrderList = redisComponent.getTimeOuterLogistics();
                    if (queueOrderList == null || queueOrderList.isEmpty()) {
                        Thread.sleep(5000);
                        continue;
                    }
                    for (String orderId : queueOrderList) {
                        if (redisComponent.removeTimeOuterLogistics(orderId) > 0) {
                            orderLogisticsInfoService.mockOrderlogistics(orderId);
                        }
                    }
                } catch (Exception e) {
                    log.error("模拟物流信息失败", e);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
    }
}
