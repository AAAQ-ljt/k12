package com.easymall.controller;

import com.easymall.annotation.GlobalInterceptor;
import com.easymall.entity.dto.PayInfoDTO;
import com.easymall.entity.dto.PostOrderDTO;
import com.easymall.entity.enums.OrderCommentStatusEnum;
import com.easymall.entity.enums.OrderStatusEnum;
import com.easymall.entity.enums.ResponseCodeEnum;
import com.easymall.entity.po.OrderInfo;
import com.easymall.entity.query.OrderInfoQuery;
import com.easymall.entity.vo.OrderCountVO;
import com.easymall.entity.vo.PaginationResultVO;
import com.easymall.entity.vo.ResponseVO;
import com.easymall.exception.BusinessException;
import com.easymall.service.OrderInfoService;
import com.easymall.service.OrderLogisticsInfoService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/order")
@Validated
public class OrderController extends ABaseController {

    @Resource
    private OrderInfoService orderInfoService;

    @Resource
    private OrderLogisticsInfoService orderLogisticsInfoService;

    @RequestMapping("/postOrder")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO postOrder(@Valid @RequestBody PostOrderDTO postOrderDTO) {
        PayInfoDTO payInfoDTO = orderInfoService.postOrder(getTokenUserInfo().getUserId(), postOrderDTO);
        return getSuccessResponseVO(payInfoDTO);
    }


    @RequestMapping("/loadMyOrder")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadMyOrder(Integer pageNo, Integer status) {
        OrderInfoQuery orderInfoQuery = new OrderInfoQuery();
        buildStatus(orderInfoQuery, status);
        orderInfoQuery.setPageNo(pageNo);
        orderInfoQuery.setUserId(getTokenUserInfo().getUserId());
        orderInfoQuery.setOrderBy("o.order_time desc");
        orderInfoQuery.setQueryItems(true);
        orderInfoQuery.setExecuteOrderStatusList(new Integer[]{OrderStatusEnum.DELETE.getStatus()});
        PaginationResultVO resultVO = orderInfoService.findListByPage(orderInfoQuery);
        return getSuccessResponseVO(resultVO);
    }

    private void buildStatus(OrderInfoQuery orderInfoQuery, Integer status) {
        if (status == null) {
            return;
        }
        OrderStatusEnum ordestStatus = OrderStatusEnum.getByStatus(status);
        if (OrderStatusEnum.DELETE == ordestStatus) {
            return;
        }
        orderInfoQuery.setOrderStatus(ordestStatus.getStatus());
        if (OrderStatusEnum.COMPLETED != ordestStatus) {
            return;
        }
        orderInfoQuery.setCommentStatus(OrderCommentStatusEnum.NOT_EVALUATED.getStatus());
    }

    @RequestMapping("/getPayInfo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO getPayInfo(@NotEmpty String orderId) {
        PayInfoDTO payInfoDTO = orderInfoService.getPayInfo(getTokenUserInfo().getUserId(), orderId);
        return getSuccessResponseVO(payInfoDTO);
    }

    @RequestMapping("/cancelOrder")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO cancelOrder(@NotEmpty String orderId) {
        orderInfoService.cancelOrder(getTokenUserInfo().getUserId(), orderId, OrderStatusEnum.CANCELLED);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/deleteOrder")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO deleteOrder(@NotEmpty String orderId) {
        orderInfoService.deleteOrder(getTokenUserInfo().getUserId(), orderId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/confirmOrder")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO confirmOrder(@NotEmpty String orderId) {
        orderInfoService.confirmOrder(getTokenUserInfo().getUserId(), orderId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/refundOrder")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO refundOrder(@NotEmpty String orderItemId) {
        orderInfoService.refundByOrderItemId(getTokenUserInfo().getUserId(), orderItemId);
        return getSuccessResponseVO(null);
    }


    @RequestMapping("/getLogistics")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO getLogistics(@NotEmpty String orderId) {
        return getSuccessResponseVO(orderLogisticsInfoService.getOrderLogisticsRecords(getTokenUserInfo().getUserId(), orderId));
    }

    @RequestMapping("/getOrderInfo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO getOrderInfo(@NotEmpty String payOrderId) {
        OrderInfoQuery orderInfoQuery = new OrderInfoQuery();
        orderInfoQuery.setPayOrderId(payOrderId);
        List<OrderInfo> orderInfoList = orderInfoService.findListByParam(orderInfoQuery);
        if (orderInfoList.isEmpty()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        OrderInfo orderInfo = orderInfoList.get(0);
        if (orderInfo == null || !orderInfo.getUserId().equals(getTokenUserInfo().getUserId())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        return getSuccessResponseVO(orderInfo);
    }

    @RequestMapping("/getOrderCountInfo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO getOrderCountInfo() {
        OrderInfoQuery orderInfoQuery = new OrderInfoQuery();
        orderInfoQuery.setUserId(getTokenUserInfo().getUserId());
        orderInfoQuery.setOrderStatus(OrderStatusEnum.WAIT_PAYMENT.getStatus());
        Integer count = orderInfoService.findCountByParam(orderInfoQuery);

        //待付款
        List<OrderCountVO> orderCountVOList = new ArrayList<>();
        OrderCountVO orderCountVO = new OrderCountVO("pendingPayment", count);
        orderCountVOList.add(orderCountVO);

        //待发货
        orderInfoQuery.setOrderStatus(OrderStatusEnum.PAID.getStatus());
        count = orderInfoService.findCountByParam(orderInfoQuery);
        orderCountVO = new OrderCountVO("pendingShipment", count);
        orderCountVOList.add(orderCountVO);

        //待收货
        orderInfoQuery.setOrderStatus(OrderStatusEnum.SHIPPED.getStatus());
        count = orderInfoService.findCountByParam(orderInfoQuery);
        orderCountVO = new OrderCountVO("pendingReceipt", count);
        orderCountVOList.add(orderCountVO);

        //待评价
        orderInfoQuery.setOrderStatus(OrderStatusEnum.COMPLETED.getStatus());
        orderInfoQuery.setCommentStatus(OrderCommentStatusEnum.NOT_EVALUATED.getStatus());
        count = orderInfoService.findCountByParam(orderInfoQuery);
        orderCountVO = new OrderCountVO("pendingComment", count);
        orderCountVOList.add(orderCountVO);

        return getSuccessResponseVO(orderCountVOList);
    }
}
