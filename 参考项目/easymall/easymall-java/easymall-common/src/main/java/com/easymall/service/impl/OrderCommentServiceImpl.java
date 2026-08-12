package com.easymall.service.impl;

import com.easymall.entity.enums.*;
import com.easymall.entity.po.OrderComment;
import com.easymall.entity.po.OrderInfo;
import com.easymall.entity.po.OrderItem;
import com.easymall.entity.query.OrderCommentQuery;
import com.easymall.entity.query.OrderInfoQuery;
import com.easymall.entity.query.OrderItemQuery;
import com.easymall.entity.query.SimplePage;
import com.easymall.entity.vo.PaginationResultVO;
import com.easymall.exception.BusinessException;
import com.easymall.mappers.OrderCommentMapper;
import com.easymall.mappers.OrderInfoMapper;
import com.easymall.mappers.OrderItemMapper;
import com.easymall.service.OrderCommentService;
import com.easymall.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


/**
 * 业务接口实现
 */
@Service("orderCommentService")
public class OrderCommentServiceImpl implements OrderCommentService {

    @Resource
    private OrderCommentMapper<OrderComment, OrderCommentQuery> orderCommentMapper;

    @Resource
    private OrderInfoMapper<OrderInfo, OrderInfoQuery> orderInfoMapper;

    @Resource
    private OrderItemMapper<OrderItem, OrderItemQuery> orderItemMapper;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<OrderComment> findListByParam(OrderCommentQuery param) {
        return this.orderCommentMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(OrderCommentQuery param) {
        return this.orderCommentMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<OrderComment> findListByPage(OrderCommentQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<OrderComment> list = this.findListByParam(param);
        PaginationResultVO<OrderComment> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(OrderComment bean) {
        return this.orderCommentMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<OrderComment> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.orderCommentMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<OrderComment> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.orderCommentMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(OrderComment bean, OrderCommentQuery param) {
        StringTools.checkParam(param);
        return this.orderCommentMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(OrderCommentQuery param) {
        StringTools.checkParam(param);
        return this.orderCommentMapper.deleteByParam(param);
    }

    /**
     * 根据OrderId获取对象
     */
    @Override
    public OrderComment getOrderCommentByOrderId(String orderId) {
        return this.orderCommentMapper.selectByOrderId(orderId);
    }

    /**
     * 根据OrderId修改
     */
    @Override
    public Integer updateOrderCommentByOrderId(OrderComment bean, String orderId) {
        return this.orderCommentMapper.updateByOrderId(bean, orderId);
    }

    /**
     * 根据OrderId删除
     */
    @Override
    public Integer deleteOrderCommentByOrderId(String orderId) {
        return this.orderCommentMapper.deleteByOrderId(orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void postComment(String userId, String orderId, String commentContent, String commentImages, Integer star) {
        OrderInfo orderInfo = orderInfoMapper.selectByOrderId(orderId);
        if (!orderInfo.getUserId().equals(userId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        if (!OrderStatusEnum.COMPLETED.getStatus().equals(orderInfo.getOrderStatus())) {
            throw new BusinessException("抱歉订单没有确认收货无法评价，请先确认收货后再评价");
        }

        if (!OrderCommentStatusEnum.NOT_EVALUATED.getStatus().equals(orderInfo.getCommentStatus())) {
            throw new BusinessException("已经评价无法再次评价");
        }
        OrderInfo updateInfo = new OrderInfo();
        updateInfo.setCommentStatus(OrderCommentStatusEnum.EVALUATED.getStatus());
        OrderInfoQuery orderInfoQuery = new OrderInfoQuery();
        orderInfoQuery.setCommentStatus(OrderCommentStatusEnum.NOT_EVALUATED.getStatus());
        orderInfoQuery.setOrderId(orderId);
        Integer updateCount = orderInfoMapper.updateByParam(updateInfo, orderInfoQuery);
        if (updateCount == 0) {
            throw new BusinessException("已经评价无法再次评价");
        }
        OrderItemQuery orderItemQuery = new OrderItemQuery();
        orderItemQuery.setOrderId(orderId);
        orderItemQuery.setOrderItemStatus(OrderItemStatusEnum.NORMAL.getStatus());
        List<OrderItem> orderItemList = orderItemMapper.selectList(orderItemQuery);

        OrderItem orderItem = orderItemList.get(0);
        OrderComment orderComment = new OrderComment();
        orderComment.setPropertyInfo(orderItem.getPropertyInfo());
        orderComment.setProductId(orderItem.getProductId());
        orderComment.setCommentContent(commentContent);
        orderComment.setCommentImages(commentImages);
        orderComment.setOrderId(orderId);
        orderComment.setUserId(userId);
        orderComment.setCommentTime(new Date());
        orderComment.setStar(star);
        orderCommentMapper.insert(orderComment);
    }

    @Transactional(rollbackFor = Exception.class)
    public void postReComment(String userId, String orderId, String reCommentContent, String reCommentImages) {
        OrderInfo orderInfo = orderInfoMapper.selectByOrderId(orderId);
        if (!orderInfo.getUserId().equals(userId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (!OrderCommentStatusEnum.EVALUATED.getStatus().equals(orderInfo.getCommentStatus())) {
            throw new BusinessException("已经追评");
        }
        OrderInfo updateInfo = new OrderInfo();
        updateInfo.setCommentStatus(OrderCommentStatusEnum.ADDITIONAL_EVALUATED.getStatus());
        OrderInfoQuery orderInfoQuery = new OrderInfoQuery();
        orderInfoQuery.setCommentStatus(OrderCommentStatusEnum.EVALUATED.getStatus());
        orderInfoQuery.setOrderId(orderId);
        Integer updateCount = orderInfoMapper.updateByParam(updateInfo, orderInfoQuery);
        if (updateCount == 0) {
            throw new BusinessException("已经追评");
        }

        OrderComment orderComment = new OrderComment();
        orderComment.setRecommentContent(reCommentContent);
        orderComment.setRecommentImages(reCommentImages);
        orderComment.setRecommentTime(new Date());
        orderCommentMapper.updateByOrderId(orderComment, orderId);
    }

    public void postBizComment(String orderId, String commentBizReply) {
        OrderInfo orderInfo = orderInfoMapper.selectByOrderId(orderId);
        if (OrderCommentStatusEnum.NOT_EVALUATED.getStatus().equals(orderInfo.getCommentStatus())) {
            throw new BusinessException("无法进行商家回复");
        }
        OrderComment orderComment = new OrderComment();
        orderComment.setCommentBizReply(commentBizReply);
        orderCommentMapper.updateByOrderId(orderComment, orderId);
    }
}