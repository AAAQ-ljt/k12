package com.easymall.controller;

import com.easymall.constants.Constants;
import com.easymall.entity.enums.DateTimePatternEnum;
import com.easymall.entity.enums.OrderStatusEnum;
import com.easymall.entity.query.OrderInfoQuery;
import com.easymall.entity.query.ProductSkuQuery;
import com.easymall.entity.query.UserInfoQuery;
import com.easymall.entity.vo.ResponseVO;
import com.easymall.entity.vo.TodayDataVO;
import com.easymall.service.ProductSkuService;
import com.easymall.service.StatisticsInfoService;
import com.easymall.service.UserInfoService;
import com.easymall.service.impl.OrderInfoServiceImpl;
import com.easymall.utils.DateUtil;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/home")
@Validated
public class HomeController extends ABaseController {

    @Resource
    private StatisticsInfoService statisticsInfoService;

    @Resource
    private OrderInfoServiceImpl orderInfoService;

    @Resource
    private ProductSkuService productSkuService;

    @Resource
    private UserInfoService userInfoService;

    /**
     * 查询当天数据
     */
    @RequestMapping("/getTodayData")
    public ResponseVO getTodayData() {
        List<TodayDataVO> result = new ArrayList<>();

        String today = DateUtil.format(new Date(), DateTimePatternEnum.YYYY_MM_DD.getPattern());
        String yesterday = DateUtil.getBeforeDay(1, DateTimePatternEnum.YYYY_MM_DD.getPattern());

        //订单金额
        BigDecimal todayOrderAmount = orderInfoService.getOrderTotalAmount(today, new Integer[]{OrderStatusEnum.PAID.getStatus(),
                OrderStatusEnum.SHIPPED.getStatus(), OrderStatusEnum.COMPLETED.getStatus()});
        BigDecimal yesterdayOrderAmount = orderInfoService.getOrderTotalAmount(yesterday, new Integer[]{OrderStatusEnum.PAID.getStatus(),
                OrderStatusEnum.SHIPPED.getStatus(), OrderStatusEnum.COMPLETED.getStatus()});
        TodayDataVO orderAmountData = new TodayDataVO("orderAmount", todayOrderAmount, yesterdayOrderAmount);
        result.add(orderAmountData);

        //订单数量
        OrderInfoQuery orderInfoQuery = new OrderInfoQuery();
        orderInfoQuery.setOrderTime(today);
        Integer todayOrderCount = orderInfoService.findCountByParam(orderInfoQuery);
        orderInfoQuery.setOrderTime(yesterday);
        Integer yesterdayOrderCount = orderInfoService.findCountByParam(orderInfoQuery);
        TodayDataVO orderCountData = new TodayDataVO("orderCount", new BigDecimal(todayOrderCount), new BigDecimal(yesterdayOrderCount));
        result.add(orderCountData);

        //用户数量
        UserInfoQuery userInfoQuery = new UserInfoQuery();
        userInfoQuery.setJoinTime(today);
        Integer todayUserCount = userInfoService.findCountByParam(userInfoQuery);
        userInfoQuery.setJoinTime(yesterday);
        Integer yesterdayUserCount = userInfoService.findCountByParam(userInfoQuery);
        TodayDataVO userCountData = new TodayDataVO("userCount", new BigDecimal(todayUserCount), new BigDecimal(yesterdayUserCount));
        result.add(userCountData);

        //退款
        BigDecimal todayRefundAmount = orderInfoService.getOrderTotalAmount(today, new Integer[]{OrderStatusEnum.REFUNDED.getStatus()});
        BigDecimal yesterdayRefundAmount = orderInfoService.getOrderTotalAmount(yesterday, new Integer[]{OrderStatusEnum.REFUNDED.getStatus()});
        TodayDataVO refundAmountData = new TodayDataVO("refundAmount", todayRefundAmount, yesterdayRefundAmount);
        result.add(refundAmountData);

        return getSuccessResponseVO(result);
    }

    /**
     * 查询最近一周统计数据
     *
     * @return
     */
    @RequestMapping("/loadWeeklyStatisticsData")
    public ResponseVO loadWeeklyStatisticsData() {
        return getSuccessResponseVO(statisticsInfoService.loadWeeklyStatisticsData());
    }

    /**
     * 库存告警商品
     *
     * @return
     */
    @RequestMapping("/loadLessStockProduct")
    public ResponseVO loadLessStockProduct() {
        ProductSkuQuery skuQuery = new ProductSkuQuery();
        skuQuery.setLessStock(Constants.LENGTH_10);
        return getSuccessResponseVO(productSkuService.findListByPage4ListVO(skuQuery));
    }
}
