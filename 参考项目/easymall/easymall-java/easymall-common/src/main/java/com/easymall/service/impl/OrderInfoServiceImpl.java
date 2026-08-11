package com.easymall.service.impl;

import com.easymall.component.EsSearchComponent;
import com.easymall.component.RedisComponent;
import com.easymall.component.SpringContext;
import com.easymall.constants.Constants;
import com.easymall.entity.config.AppConfig;
import com.easymall.entity.dto.*;
import com.easymall.entity.enums.*;
import com.easymall.entity.po.*;
import com.easymall.entity.query.*;
import com.easymall.entity.vo.PaginationResultVO;
import com.easymall.exception.BusinessException;
import com.easymall.mappers.*;
import com.easymall.service.OrderInfoService;
import com.easymall.service.PayChannel;
import com.easymall.utils.StringTools;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 订单信息 业务接口实现
 */
@Service("orderInfoService")
public class OrderInfoServiceImpl implements OrderInfoService {

    @Resource
    private OrderInfoMapper<OrderInfo, OrderInfoQuery> orderInfoMapper;

    @Resource
    private ProductInfoMapper<ProductInfo, ProductInfoQuery> productInfoMapper;

    @Resource
    private ProductSkuMapper<ProductSku, ProductSkuQuery> productSkuMapper;

    @Resource
    private ProductPropertyValueMapper<ProductPropertyValue, ProductPropertyValueQuery> productPropertyValueMapper;

    @Resource
    private OrderItemMapper<OrderItem, OrderItemQuery> orderItemMapper;

    @Resource
    private ProductCartMapper<ProductCart, ProductCartQuery> productCartMapper;

    @Resource
    private OrderLogisticsInfoMapper<OrderLogisticsInfo, OrderLogisticsInfoQuery> orderLogisticsInfoMapper;

    @Resource
    private OrderLogisticsInfoRecordMapper<OrderLogisticsInfoRecord, OrderLogisticsInfoRecordQuery> orderLogisticsInfoRecordMapper;

    @Resource
    private UserAddressMapper<UserAddress, UserAddressQuery> userAddressMapper;

    @Resource
    private AppConfig appConfig;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private EsSearchComponent esSearchComponent;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<OrderInfo> findListByParam(OrderInfoQuery param) {
        return this.orderInfoMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(OrderInfoQuery param) {
        return this.orderInfoMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<OrderInfo> findListByPage(OrderInfoQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<OrderInfo> list = this.findListByParam(param);
        PaginationResultVO<OrderInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(OrderInfo bean) {
        return this.orderInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<OrderInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.orderInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<OrderInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.orderInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(OrderInfo bean, OrderInfoQuery param) {
        StringTools.checkParam(param);
        return this.orderInfoMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(OrderInfoQuery param) {
        StringTools.checkParam(param);
        return this.orderInfoMapper.deleteByParam(param);
    }

    /**
     * 根据OrderId获取对象
     */
    @Override
    public OrderInfo getOrderInfoByOrderId(String orderId) {
        return this.orderInfoMapper.selectByOrderId(orderId);
    }

    /**
     * 根据OrderId修改
     */
    @Override
    public Integer updateOrderInfoByOrderId(OrderInfo bean, String orderId) {
        return this.orderInfoMapper.updateByOrderId(bean, orderId);
    }

    /**
     * 根据OrderId删除
     */
    @Override
    public Integer deleteOrderInfoByOrderId(String orderId) {
        return this.orderInfoMapper.deleteByOrderId(orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayInfoDTO postOrder(String userId, PostOrderDTO postOrderDTO) {
        PayChannelEnum payChannelEnum = PayChannelEnum.getByPayScene(postOrderDTO.getPayMethod());
        if (payChannelEnum == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        OrderFromTypeEnum orderFromTypeEnum = OrderFromTypeEnum.getByType(postOrderDTO.getOrderFrom());
        if (orderFromTypeEnum == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        List<PostOrderItemDTO> itemDTOList = postOrderDTO.getOrderList();

        List<String> productIdList = itemDTOList.stream().map(PostOrderItemDTO::getProductId).toList();

        //查询地址信息 最简单的查询
        UserAddress userAddress = userAddressMapper.selectByAddressId(postOrderDTO.getAddressId());
        if (userAddress == null || !userAddress.getUserId().equals(userId)) {
            throw new BusinessException("地址信息不存在");
        }
        //查询商品信息
        ProductInfoQuery productInfoQuery = new ProductInfoQuery();
        productInfoQuery.setProductIdList(productIdList);
        List<ProductInfo> productInfoList = productInfoMapper.selectList(productInfoQuery);
        Map<String, ProductInfo> tempProductInfoMap = productInfoList.stream().collect(Collectors.toMap(item -> item.getProductId(), Function.identity(), (data1,
                                                                                                                                                           data2) -> data2));
        //查询商品属性信息
        ProductPropertyValueQuery productPropertyValueQuery = new ProductPropertyValueQuery();
        productPropertyValueQuery.setProductIdList(productIdList);
        List<ProductPropertyValue> productPropertyValueList = productPropertyValueMapper.selectList(productPropertyValueQuery);
        Map<String, ProductPropertyValue> productPropertyValueMap =
                productPropertyValueList.stream().collect(Collectors.toMap(item -> item.getProductId() + item.getPropertyValueId(), Function.identity(),
                        (data1, data2) -> data2));
        //查询sku信息
        ProductSkuQuery productSkuQuery = new ProductSkuQuery();
        productSkuQuery.setProductIdList(productIdList);
        List<ProductSku> productSkuList = productSkuMapper.selectList(productSkuQuery);
        Map<String, ProductSku> productSkuMap = productSkuList.stream().collect(Collectors.toMap(item -> item.getProductId() + item.getPropertyValueIds(),
                Function.identity(), (data1, data2) -> data2));


        Date curDate = new Date();
        //主订单
        List<OrderInfo> orderList = new ArrayList<>();
        //订单详情
        List<OrderItem> orderItemList = new ArrayList<>();
        //购物车信息
        List<ProductCart> productCartList = new ArrayList<>();
        //订单物流信息
        List<OrderLogisticsInfo> orderLogisticsInfoList = new ArrayList<>();

        Map<String, OrderInfo> orderInfoMap = new HashMap<>();

        String payOrderId = StringTools.createPayOrderId();

        //发货信息
        LogisticsSendDTO sendDTO = redisComponent.getLogisticsInfo();

        for (PostOrderItemDTO itemDTO : itemDTOList) {
            ProductInfo productInfo = tempProductInfoMap.get(itemDTO.getProductId());
            if (productInfo == null || !ProductStatusEnum.ON_SALE.getStatus().equals(productInfo.getStatus())) {
                throw new BusinessException("商品不存在或已下架");
            }
            String propertyValueIds = itemDTO.getPropertyValueIds();
            String[] propertyValueIdArray = propertyValueIds.split("-");
            List<String> propertyData = new ArrayList<>();
            String cover = null;
            for (String propertyValueId : propertyValueIdArray) {
                ProductPropertyValue productPropertyValue = productPropertyValueMap.get(itemDTO.getProductId() + propertyValueId);
                if (productPropertyValue == null) {
                    throw new BusinessException("商品属性不存在");
                }
                propertyData.add(productPropertyValue.getPropertyName() + ":" + productPropertyValue.getPropertyValue());
                if (cover == null && !StringTools.isEmpty(productPropertyValue.getPropertyCover())) {
                    cover = productPropertyValue.getPropertyCover();
                }
            }

            ProductSku productSku = productSkuMap.get(itemDTO.getProductId() + propertyValueIds);
            if (productSku == null) {
                throw new BusinessException("商品sku不存在");
            }
            if (productSku.getStock() < itemDTO.getBuyCount()) {
                throw new BusinessException("商品【" + productInfo.getProductName() + "】库存不足");
            }
            OrderInfo orderInfo = orderInfoMap.get(itemDTO.getProductId());
            if (orderInfo == null) {
                orderInfo = new OrderInfo();
                orderInfo.setOrderId(StringTools.createProductOrderId());
                orderInfo.setOrderTime(curDate);
                orderInfo.setUserId(userId);
                orderInfo.setOrderStatus(OrderStatusEnum.WAIT_PAYMENT.getStatus());
                orderInfo.setAmount(new BigDecimal(Constants.ZERO_STR));
                orderInfo.setCommentStatus(OrderCommentStatusEnum.NOT_EVALUATED.getStatus());
                orderInfo.setPayChannel(payChannelEnum.getPayChannel());
                orderInfo.setPayScene(payChannelEnum.getPayScene());
                orderInfo.setPayOrderId(payOrderId);

                orderInfoMap.put(itemDTO.getProductId(), orderInfo);
                orderList.add(orderInfo);

                List<OrderItem> orderItems = new ArrayList<>();
                orderInfo.setOrderItemList(orderItems);

                //记录地址信息
                OrderLogisticsInfo orderLogisticsInfo = new OrderLogisticsInfo();
                orderLogisticsInfo.setOrderId(orderInfo.getOrderId());
                orderLogisticsInfo.setUserId(userId);
                orderLogisticsInfo.setLogisticsStatus(LogisticsStatusEnum.PENDING_SHIPMENT.getStatus());
                orderLogisticsInfo.setReceiverName(userAddress.getAddressee());
                orderLogisticsInfo.setReceiverPhone(userAddress.getPhone());
                orderLogisticsInfo.setReceiverAddress(userAddress.getAddress());

                //设置默认发货地址
                if (sendDTO != null) {
                    orderLogisticsInfo.setSenderAddress(sendDTO.getSenderAddress());
                    orderLogisticsInfo.setSenderName(sendDTO.getSenderName());
                    orderLogisticsInfo.setSenderPhone(sendDTO.getSenderPhone());
                }
                orderLogisticsInfoList.add(orderLogisticsInfo);


            }
            cover = cover == null ? productInfo.getCover().split(",")[0] : cover;
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderItemId(orderInfo.getOrderId() + "_" + (orderInfo.getOrderItemList().size() + 1));
            orderItem.setOrderId(orderInfo.getOrderId());
            orderItem.setCover(cover);
            orderItem.setProductId(itemDTO.getProductId());
            orderItem.setProductName(productInfo.getProductName());
            orderItem.setPropertyValueIdHash(StringTools.encodeByMD5(propertyValueIds));
            orderItem.setPropertyInfo(String.join(";", propertyData));
            orderItem.setItemAmount(productSku.getPrice().multiply(new BigDecimal(itemDTO.getBuyCount())));
            orderItem.setBuyCount(itemDTO.getBuyCount());
            orderItem.setOrderItemStatus(OrderItemStatusEnum.NORMAL.getStatus());
            orderItem.setRemark(itemDTO.getRemark());

            orderInfo.setAmount(orderInfo.getAmount().add(orderItem.getItemAmount()));


            orderItemList.add(orderItem);
            //添加到订单中
            orderInfo.getOrderItemList().add(orderItem);

            //删除购物车
            if (OrderFromTypeEnum.CART == orderFromTypeEnum) {
                ProductCart productCart = new ProductCart();
                productCart.setUserId(userId);
                productCart.setProductId(productInfo.getProductId());
                productCart.setPropertyValueIds(propertyValueIds);
                productCart.setPropertyValueIdHash(StringTools.encodeByMD5(propertyValueIds));
                productCartList.add(productCart);
            }

        }
        //扣减库存
        Integer updateCount = productSkuMapper.updateStockBatch(orderItemList);
        if (updateCount != orderItemList.size()) {
            throw new BusinessException("库存不足");
        }
        this.orderInfoMapper.insertBatch(orderList);
        this.orderItemMapper.insertBatch(orderItemList);

        //记录物流信息
        orderLogisticsInfoMapper.insertBatch(orderLogisticsInfoList);

        //如果是购物车，清除相关数据
        if (OrderFromTypeEnum.CART == orderFromTypeEnum) {
            productCartMapper.deleteBatch(productCartList);
        }

        //获取支付信息
        PayChannel payChannel = (PayChannel) SpringContext.getBean(payChannelEnum.getBeanName());
        String subject = OrderFromTypeEnum.CART == orderFromTypeEnum ? String.format(Constants.CART_PAY_NAME, orderList.size()) :
                orderList.get(0).getOrderItemList().get(0).getProductName();
        BigDecimal amount = orderList.stream().map(OrderInfo::getAmount).reduce(BigDecimal::add).get();
        PayInfoDTO payInfoDTO = payChannel.getPayUrl(payChannelEnum, payOrderId, subject, amount);
        //将订单放入延时队列
        for (OrderInfo orderInfo : orderList) {
            redisComponent.addOrder2DelayQueue(Constants.REDIS_KEY_ORDER_DELAY_QUEUE, appConfig.getOrderExpireMinute(), orderInfo.getOrderId());
        }
        return payInfoDTO;
    }

    @Override
    public void payNotify(PayChannelEnum payChannelEnum, Map<String, String> requestParams, String jsonBody) {
        PayChannel channe = (PayChannel) SpringContext.getBean(payChannelEnum.getBeanName());
        PayOrderNotifyDTO payOrderNotifyDTO = channe.payNotify(requestParams, null);
        if (payOrderNotifyDTO == null) {
            return;
        }
        //回调处理
        payOrderSuccess(payOrderNotifyDTO);
    }

    @Override
    public void payOrderSuccess(PayOrderNotifyDTO payOrderNotifyDTO) {
        //更新商品订单状态
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderStatus(OrderStatusEnum.PAID.getStatus());
        orderInfo.setChannelOrderId(payOrderNotifyDTO.getChannelOrderId());

        OrderInfoQuery orderInfoQuery = new OrderInfoQuery();
        orderInfoQuery.setPayOrderId(payOrderNotifyDTO.getPayOrderId());
        orderInfoQuery.setOrderStatus(OrderStatusEnum.WAIT_PAYMENT.getStatus());
        orderInfoMapper.updateByParam(orderInfo, orderInfoQuery);

        //支付成功，自动发货队列，这里是为了方便延时，这里可以去掉，后台手动来发货
        redisComponent.addOrder2DelayQueue(Constants.REDIS_KEY_ORDER_DELAY_QUEUE_DELIVERY, 1, payOrderNotifyDTO.getPayOrderId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String userId, String orderId, OrderStatusEnum orderStatusEnum) {
        OrderInfo orderInfo = orderInfoMapper.selectByOrderId(orderId);
        if (orderInfo == null) {
            throw new BusinessException("订单不存在");
        }

        if (!OrderStatusEnum.WAIT_PAYMENT.getStatus().equals(orderInfo.getOrderStatus())) {
            throw new BusinessException("订单已经支付无法取消");
        }

        if (null != userId && !orderInfo.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }

        //取消所有的订单
        OrderInfoQuery orderInfoQuery = new OrderInfoQuery();
        orderInfoQuery.setPayOrderId(orderInfo.getPayOrderId());
        List<OrderInfo> orderList = orderInfoMapper.selectList(orderInfoQuery);
        List<String> orderIdList = orderList.stream().map(OrderInfo::getOrderId).collect(Collectors.toList());
        Integer updateCount = orderInfoMapper.updateOrderStatusBatch(orderStatusEnum.getStatus(), OrderStatusEnum.WAIT_PAYMENT.getStatus(), orderIdList);
        if (updateCount != orderIdList.size()) {
            throw new BusinessException("订单已经支付无法取消");
        }

        //退所有的库存
        OrderItemQuery orderItemQuery = new OrderItemQuery();
        orderItemQuery.setOrderIdList(orderIdList);
        List<OrderItem> orderItemList = orderItemMapper.selectList(orderItemQuery);
        productSkuMapper.updateStockBatch(orderItemList);


        cancelOrder4Channel(orderInfo);
    }

    private void cancelOrder4Channel(OrderInfo orderInfo) {
        PayChannelEnum payChannelEnum = PayChannelEnum.getByPayScene(orderInfo.getPayScene());
        PayChannel payChannel = (PayChannel) SpringContext.getBean(payChannelEnum.getBeanName());
        payChannel.closeOrder(orderInfo.getPayOrderId());
    }

    //获取支付信息
    public PayInfoDTO getPayInfo(String userId, String orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectByOrderId(orderId);
        if (orderInfo == null || !orderInfo.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }

        if (orderInfo.getOrderStatus() != OrderStatusEnum.WAIT_PAYMENT.getStatus()) {
            throw new BusinessException("订单已支付");
        }
        //生成新的订单
        PayChannelEnum payChannelEnum = PayChannelEnum.getByPayScene(orderInfo.getPayScene());
        PayChannel payChannel = (PayChannel) SpringContext.getBean(payChannelEnum.getBeanName());
        String subject = orderInfo.getOrderId();
        BigDecimal amount = orderInfo.getAmount();
        String payOrderId = StringTools.createPayOrderId();

        PayInfoDTO payInfoDTO = payChannel.getPayUrl(payChannelEnum, payOrderId, subject, amount);
        //将之前的订单取消
        cancelOrder4Channel(orderInfo);

        //更新支付订单ID
        OrderInfo updateOrderInfo = new OrderInfo();
        updateOrderInfo.setPayOrderId(payOrderId);
        orderInfoMapper.updateByOrderId(updateOrderInfo, orderInfo.getOrderId());
        return payInfoDTO;
    }

    @Override
    public void deleteOrder(String userId, String orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectByOrderId(orderId);
        if (orderInfo == null || !orderInfo.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }

        if (!ArrayUtils.contains(new Integer[]{OrderStatusEnum.CANCELLED.getStatus(), OrderStatusEnum.CLOSED.getStatus(), OrderStatusEnum.COMPLETED.getStatus()},
                orderInfo.getOrderStatus())) {
            throw new BusinessException("订单无法删除");
        }

        OrderInfo updateOrderInfo = new OrderInfo();
        updateOrderInfo.setOrderStatus(OrderStatusEnum.DELETE.getStatus());

        OrderInfoQuery orderInfoQuery = new OrderInfoQuery();
        orderInfoQuery.setOrderId(orderId);
        orderInfoQuery.setOrderStatusList(new Integer[]{OrderStatusEnum.CANCELLED.getStatus(), OrderStatusEnum.CLOSED.getStatus(),
                OrderStatusEnum.COMPLETED.getStatus()});

        this.orderInfoMapper.updateByParam(updateOrderInfo, orderInfoQuery);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmOrder(String userId, String orderId) {
        OrderInfo updateOrderInfo = new OrderInfo();
        updateOrderInfo.setOrderStatus(OrderStatusEnum.COMPLETED.getStatus());

        OrderInfoQuery orderInfoQuery = new OrderInfoQuery();
        orderInfoQuery.setOrderId(orderId);
        orderInfoQuery.setUserId(userId);
        orderInfoQuery.setOrderStatusList(new Integer[]{OrderStatusEnum.SHIPPED.getStatus(), OrderStatusEnum.PARTIALLY_REFUNDED.getStatus()});
        Integer updateCount = this.orderInfoMapper.updateByParam(updateOrderInfo, orderInfoQuery);
        if (updateCount == 0) {
            throw new BusinessException("该订单当前状态无法确认收货");
        }

        //增加商品销售量
        OrderItemQuery orderItemQuery = new OrderItemQuery();
        orderItemQuery.setOrderId(orderId);
        List<OrderItem> orderItemList = orderItemMapper.selectList(orderItemQuery);
        Integer buyCount = orderItemList.stream().mapToInt(OrderItem::getBuyCount).sum();
        this.productInfoMapper.updateProdctuctTotalSale(orderItemList.get(0).getProductId(), buyCount);

        //更新es销量
        esSearchComponent.saveProduct(orderItemList.get(0).getProductId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundByOrderItemId(String userId, String orderItemId) {
        OrderItem orderItem = orderItemMapper.selectByOrderItemId(orderItemId);
        if (orderItem == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        OrderInfo orderInfo = orderInfoMapper.selectByOrderId(orderItem.getOrderId());
        if (orderInfo == null || !orderInfo.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        Integer[] canRefundStatus = new Integer[]{OrderStatusEnum.PAID.getStatus(), OrderStatusEnum.SHIPPED.getStatus(), OrderStatusEnum.PARTIALLY_REFUNDED.getStatus()};

        if (!ArrayUtils.contains(canRefundStatus, orderInfo.getOrderStatus())) {
            throw new BusinessException("订单无法退款");
        }

        if (!OrderItemStatusEnum.NORMAL.getStatus().equals(orderItem.getOrderItemStatus())) {
            throw new BusinessException("订单已经退款无法再次退款");
        }

        String refundOrderId = StringTools.getRandomNumber(Constants.LENGTH_30);
        OrderItem updateOrderItem = new OrderItem();
        updateOrderItem.setOrderItemStatus(OrderItemStatusEnum.REFUND.getStatus());
        updateOrderItem.setRefundOrderId(refundOrderId);

        OrderItemQuery orderItemQuery = new OrderItemQuery();
        orderItemQuery.setOrderItemId(orderItemId);
        orderItemQuery.setOrderItemStatus(OrderItemStatusEnum.NORMAL.getStatus());

        Integer updateCount = this.orderItemMapper.updateByParam(updateOrderItem, orderItemQuery);
        if (updateCount == 0) {
            throw new BusinessException("退款失败，请稍后再试");
        }
        orderItemQuery = new OrderItemQuery();
        orderItemQuery.setOrderId(orderItem.getOrderId());
        orderItemQuery.setOrderItemStatus(OrderItemStatusEnum.NORMAL.getStatus());
        Integer leftCount = this.orderItemMapper.selectCount(orderItemQuery);

        //更新主订单
        OrderInfo updateInfo = new OrderInfo();
        updateInfo.setOrderStatus(leftCount == 0 ? OrderStatusEnum.REFUNDED.getStatus() : OrderStatusEnum.PARTIALLY_REFUNDED.getStatus());

        OrderInfoQuery orderInfoQuery = new OrderInfoQuery();
        orderInfoQuery.setOrderId(orderItem.getOrderId());
        orderInfoQuery.setOrderStatusList(canRefundStatus);

        Integer updateOrderCount = this.orderInfoMapper.updateByParam(updateInfo, orderInfoQuery);
        if (updateOrderCount == 0) {
            throw new BusinessException("退款失败，请稍后再试");
        }
        PayChannelEnum payChannelEnum = PayChannelEnum.getByPayScene(orderInfo.getPayScene());
        PayChannel payChannel = (PayChannel) SpringContext.getBean(payChannelEnum.getBeanName());
        //调用支付宝退款
        payChannel.refund(orderInfo.getPayOrderId(), refundOrderId, orderItem.getItemAmount());
    }

    @Override
    public void refundByOrderId(String userId, String orderId) {
        //查询所有子订单
        OrderItemQuery orderItemQuery = new OrderItemQuery();
        orderItemQuery.setOrderId(orderId);
        List<OrderItem> orderItemList = this.orderItemMapper.selectList(orderItemQuery);
        if (orderItemList.isEmpty()) {
            throw new BusinessException("没有获取到对应的订单信息，请输入正确的订单号进行退款");
        }
        for (OrderItem orderItem : orderItemList) {
            if (OrderItemStatusEnum.REFUND.getStatus().equals(orderItem.getOrderItemStatus())) {
                continue;
            }
            refundByOrderItemId(userId, orderItem.getOrderItemId());
        }
    }

    @Override
    public BigDecimal getOrderTotalAmount(String orderTime, Integer[] orderStatus) {
        return this.orderInfoMapper.selectOrderTotalAmount(orderTime, orderStatus);
    }
}