package com.easymall.controller;

import com.easymall.entity.dto.OrderStatusDTO;
import com.easymall.entity.enums.CommentStatusEnum;
import com.easymall.entity.enums.OrderStatusEnum;
import com.easymall.entity.po.OrderComment;
import com.easymall.entity.po.OrderInfo;
import com.easymall.entity.po.OrderLogisticsInfo;
import com.easymall.entity.query.OrderCommentQuery;
import com.easymall.entity.query.OrderInfoQuery;
import com.easymall.entity.vo.PaginationResultVO;
import com.easymall.entity.vo.ResponseVO;
import com.easymall.service.OrderCommentService;
import com.easymall.service.OrderInfoService;
import com.easymall.service.OrderLogisticsInfoService;
import com.easymall.service.impl.UserInfoServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/order")
@Validated
public class OrderController extends ABaseController {

    @Resource
    private OrderInfoService orderInfoService;

    @Resource
    private OrderLogisticsInfoService orderLogisticsInfoService;

    @Resource
    private OrderCommentService orderCommentService;

    @RequestMapping("/loadOrderStatus")
    public ResponseVO loadOrderStatus() {
        return getSuccessResponseVO(Arrays.stream(OrderStatusEnum.values()).map(OrderStatusDTO::from).collect(Collectors.toList()));
    }

    @RequestMapping("/loadOrder")
    public ResponseVO loadOrder(OrderInfoQuery orderInfoQuery) {
        orderInfoQuery.setOrderBy("o.order_time desc");
        orderInfoQuery.setQueryItems(true);
        orderInfoQuery.setQueryUser(true);
        PaginationResultVO<OrderInfo> resultVO = orderInfoService.findListByPage(orderInfoQuery);
        return getSuccessResponseVO(resultVO);
    }

    @RequestMapping("/getLogistics")
    public ResponseVO getLogistics(String orderId) {
        OrderLogisticsInfo orderLogisticsInfo = orderLogisticsInfoService.getOrderLogisticsInfoByOrderId(orderId);
        return getSuccessResponseVO(orderLogisticsInfo);
    }

    @RequestMapping("/delivery")
    public ResponseVO delivery(OrderLogisticsInfo logisticsInfo) {
        orderLogisticsInfoService.delivery(logisticsInfo);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/getComment")
    public ResponseVO getComment(String orderId) {
        OrderComment orderComment = orderCommentService.getOrderCommentByOrderId(orderId);
        return getSuccessResponseVO(orderComment);
    }

    @RequestMapping("/bizComment")
    public ResponseVO bizComment(String orderId, String commentBizReply) {
        orderCommentService.postBizComment(orderId, commentBizReply);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadComment")
    public ResponseVO loadComment(OrderCommentQuery query) {
        query.setOrderBy("o.comment_time desc");
        query.setQueryProduct(true);
        query.setQueryUserInfo(true);
        return getSuccessResponseVO(orderCommentService.findListByPage(query));
    }

    @RequestMapping("/delComment")
    public ResponseVO delComment(String orderId) {
        OrderComment orderComment = new OrderComment();
        orderComment.setStatus(CommentStatusEnum.DEL.getStatus());

        OrderCommentQuery orderCommentQuery = new OrderCommentQuery();
        orderCommentQuery.setOrderId(orderId);

        orderCommentService.updateByParam(orderComment, orderCommentQuery);
        return getSuccessResponseVO(null);
    }
}
