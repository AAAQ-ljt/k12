package com.easymall.service;

import com.easymall.entity.enums.OrderStatusEnum;
import com.easymall.entity.enums.ResponseCodeEnum;
import com.easymall.entity.po.OrderLogisticsInfo;
import com.easymall.exception.BusinessException;
import com.easymall.service.OrderCommentService;
import com.easymall.service.OrderInfoService;
import com.easymall.service.OrderLogisticsInfoService;
import com.easymall.utils.JsonUtils;
import com.easymall.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderService {

    @Resource
    private OrderInfoService orderInfoService;

    @Resource
    private OrderLogisticsInfoService orderLogisticsInfoService;

    @Resource
    private OrderCommentService orderCommentService;

    @Tool(name = "refund", description = "退款")
    public String refund(@ToolParam(description = "用户ID") String userId, @ToolParam(description = "订单ID") String orderId) {
        if (StringTools.isEmpty(orderId)) {
            return "请提供完整的订单号";
        }
        try {
            orderInfoService.refundByOrderId(userId, orderId);
        } catch (BusinessException e) {
            return e.getMessage();
        } catch (Exception e) {
            return ResponseCodeEnum.CODE_500.getMsg();
        }
        return "退款成功";
    }

    @Tool(name = "orderConfirm", description = "确认收货")
    public String orderConfirm(@ToolParam(description = "用户ID") String userId,
                               @ToolParam(description = "订单ID") String orderId) {
        try {
            orderInfoService.confirmOrder(userId, orderId);
            return "确认收货成功";
        } catch (BusinessException e) {
            return e.getMessage();
        } catch (Exception e) {
            return "系统异常";
        }
    }

    @Tool(name = "cancelOrder", description = "取消订单")
    public String cancelOrder(@ToolParam(description = "用户ID") String userId,
                              @ToolParam(description = "订单ID") String orderId) {
        try {
            orderInfoService.cancelOrder(userId, orderId, OrderStatusEnum.CANCELLED);
            return "订单取消成功";
        } catch (BusinessException e) {
            return e.getMessage();
        } catch (Exception e) {
            log.error("取消订单失败", e);
            return "系统异常";
        }
    }

    @Tool(name = "queryOrderLogistics", description = "查询订单物流信息")
    public String queryOrderLogistics(@ToolParam(description = "用户ID") String userId, @ToolParam(description = "订单ID") String orderId) {
        try {
            if (StringTools.isEmpty(userId) || StringTools.isEmpty(orderId)) {
                return "请提供详细的订单ID";
            }
            OrderLogisticsInfo orderLogisticsInfo = orderLogisticsInfoService.getOrderLogisticsRecords(userId, orderId);
            return JsonUtils.convertObj2Json(orderLogisticsInfo);
        } catch (BusinessException e) {
            log.error("查询物流失败", e);
            return e.getMessage();
        } catch (Exception e) {
            log.error("查询物流失败", e);
            return "查询物流失败";
        }
    }

    @Tool(name = "productReview", description = "商品评价")
    public String productReview(@ToolParam(description = "用户ID") String userId,
                                @ToolParam(description = "订单ID") String orderId,
                                @ToolParam(description = "评价内容") String commentContent,
                                @ToolParam(description = "评价星级1-5级") Integer star) {
        try {
            orderCommentService.postComment(userId, orderId, commentContent, null, star);
            return "评价成功";
        } catch (BusinessException e) {
            return e.getMessage();
        } catch (Exception e) {
            log.error("评价失败", e);
            return "系统异常";
        }
    }
}
