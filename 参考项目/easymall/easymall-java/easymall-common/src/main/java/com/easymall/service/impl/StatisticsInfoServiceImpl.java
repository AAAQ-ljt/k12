package com.easymall.service.impl;

import com.easymall.entity.enums.DateTimePatternEnum;
import com.easymall.entity.enums.OrderStatusEnum;
import com.easymall.entity.enums.PageSize;
import com.easymall.entity.enums.StatisticsDataTypeEnum;
import com.easymall.entity.po.StatisticsInfo;
import com.easymall.entity.query.OrderInfoQuery;
import com.easymall.entity.query.SimplePage;
import com.easymall.entity.query.StatisticsInfoQuery;
import com.easymall.entity.vo.PaginationResultVO;
import com.easymall.entity.vo.StatisticsDataVO;
import com.easymall.mappers.StatisticsInfoMapper;
import com.easymall.service.OrderInfoService;
import com.easymall.service.StatisticsInfoService;
import com.easymall.utils.DateUtil;
import com.easymall.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 数据统计结果 业务接口实现
 */
@Service("statisticsInfoService")
public class StatisticsInfoServiceImpl implements StatisticsInfoService {

    @Resource
    private StatisticsInfoMapper<StatisticsInfo, StatisticsInfoQuery> statisticsInfoMapper;

    @Resource
    private OrderInfoService orderInfoService;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<StatisticsInfo> findListByParam(StatisticsInfoQuery param) {
        return this.statisticsInfoMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(StatisticsInfoQuery param) {
        return this.statisticsInfoMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<StatisticsInfo> findListByPage(StatisticsInfoQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<StatisticsInfo> list = this.findListByParam(param);
        PaginationResultVO<StatisticsInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(StatisticsInfo bean) {
        return this.statisticsInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<StatisticsInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.statisticsInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<StatisticsInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.statisticsInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(StatisticsInfo bean, StatisticsInfoQuery param) {
        StringTools.checkParam(param);
        return this.statisticsInfoMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(StatisticsInfoQuery param) {
        StringTools.checkParam(param);
        return this.statisticsInfoMapper.deleteByParam(param);
    }

    /**
     * 根据StatisticsDateAndDataType获取对象
     */
    @Override
    public StatisticsInfo getStatisticsInfoByStatisticsDateAndDataType(String statisticsDate, Integer dataType) {
        return this.statisticsInfoMapper.selectByStatisticsDateAndDataType(statisticsDate, dataType);
    }

    /**
     * 根据StatisticsDateAndDataType修改
     */
    @Override
    public Integer updateStatisticsInfoByStatisticsDateAndDataType(StatisticsInfo bean, String statisticsDate, Integer dataType) {
        return this.statisticsInfoMapper.updateByStatisticsDateAndDataType(bean, statisticsDate, dataType);
    }

    /**
     * 根据StatisticsDateAndDataType删除
     */
    @Override
    public Integer deleteStatisticsInfoByStatisticsDateAndDataType(String statisticsDate, Integer dataType) {
        return this.statisticsInfoMapper.deleteByStatisticsDateAndDataType(statisticsDate, dataType);
    }

    @Override
    public List<StatisticsDataVO> loadWeeklyStatisticsData() {
        StatisticsInfoQuery query = new StatisticsInfoQuery();
        String startDate = DateUtil.getBeforeDay(8, DateTimePatternEnum.YYYY_MM_DD.getPattern());
        String endDate = DateUtil.getBeforeDay(1, DateTimePatternEnum.YYYY_MM_DD.getPattern());
        query.setStatisticsDateStart(startDate);
        query.setStatisticsDateEnd(endDate);
        query.setOrderBy("statistics_date asc,data_type asc");
        List<StatisticsInfo> statisticsInfoList = statisticsInfoMapper.selectList(query);

        Map<String, StatisticsInfo> statisticsInfoMap = statisticsInfoList.stream().collect(Collectors.toMap(item -> item.getStatisticsDate() + item.getDataType(),
                Function.identity(), (data1, data2) -> data2));
        List<String> dateList = DateUtil.getDateRange(startDate, endDate, DateTimePatternEnum.YYYY_MM_DD.getPattern());
        List<StatisticsDataVO> statisticsDataVOList = new ArrayList<>();

        for (StatisticsDataTypeEnum dataType : StatisticsDataTypeEnum.values()) {
            StatisticsDataVO statisticsDataVO = new StatisticsDataVO();
            statisticsDataVOList.add(statisticsDataVO);

            statisticsDataVO.setDataType(dataType.getType());
            for (String date : dateList) {
                StatisticsInfo statisticsInfo = statisticsInfoMap.get(date + dataType.getType());
                statisticsDataVO.getDateList().add(date);
                if (statisticsInfo == null) {
                    statisticsDataVO.getDataList().add(BigDecimal.ZERO);
                } else {
                    statisticsDataVO.getDataList().add(statisticsInfo.getDataValue());
                }
            }
        }
        return statisticsDataVOList;
    }

    @Override
    public void statisticsData(String date) {

        List<StatisticsInfo> statisticsInfoList = new ArrayList<>();
        //订单金额
        BigDecimal yesterdayOrderAmount = orderInfoService.getOrderTotalAmount(date, new Integer[]{OrderStatusEnum.PAID.getStatus(),
                OrderStatusEnum.SHIPPED.getStatus(), OrderStatusEnum.COMPLETED.getStatus()});
        StatisticsInfo statisticsInfo = new StatisticsInfo();
        statisticsInfo.setStatisticsDate(date);
        statisticsInfo.setDataType(StatisticsDataTypeEnum.SALE_AMOUNT.getType());
        statisticsInfo.setDataValue(yesterdayOrderAmount);
        statisticsInfoList.add(statisticsInfo);

        //退款
        BigDecimal yesterdayRefundAmount = orderInfoService.getOrderTotalAmount(date, new Integer[]{OrderStatusEnum.REFUNDED.getStatus()});
        statisticsInfo = new StatisticsInfo();
        statisticsInfo.setStatisticsDate(date);
        statisticsInfo.setDataType(StatisticsDataTypeEnum.REFUND_AMOUNT.getType());
        statisticsInfo.setDataValue(yesterdayRefundAmount);
        statisticsInfoList.add(statisticsInfo);

        //订单数量
        OrderInfoQuery orderInfoQuery = new OrderInfoQuery();
        orderInfoQuery.setOrderTime(date);
        orderInfoQuery.setOrderStatusList(new Integer[]{OrderStatusEnum.PAID.getStatus(),
                OrderStatusEnum.SHIPPED.getStatus(), OrderStatusEnum.COMPLETED.getStatus()});
        Integer yesterdayOrderCount = orderInfoService.findCountByParam(orderInfoQuery);
        statisticsInfo = new StatisticsInfo();
        statisticsInfo.setStatisticsDate(date);
        statisticsInfo.setDataType(StatisticsDataTypeEnum.SALE_COUNT.getType());
        statisticsInfo.setDataValue(new BigDecimal(yesterdayOrderCount));
        statisticsInfoList.add(statisticsInfo);

        orderInfoQuery.setOrderStatusList(new Integer[]{OrderStatusEnum.REFUNDED.getStatus()});
        Integer yesterdayRefundOrderCount = orderInfoService.findCountByParam(orderInfoQuery);
        statisticsInfo = new StatisticsInfo();
        statisticsInfo.setStatisticsDate(date);
        statisticsInfo.setDataType(StatisticsDataTypeEnum.REFUND_COUNT.getType());
        statisticsInfo.setDataValue(new BigDecimal(yesterdayRefundOrderCount));
        statisticsInfoList.add(statisticsInfo);

        statisticsInfoMapper.insertOrUpdateBatch(statisticsInfoList);

    }
}