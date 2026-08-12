package com.easymall.service;

import com.easymall.entity.po.StatisticsInfo;
import com.easymall.entity.query.StatisticsInfoQuery;
import com.easymall.entity.vo.PaginationResultVO;
import com.easymall.entity.vo.StatisticsDataVO;

import java.util.List;


/**
 * 数据统计结果 业务接口
 */
public interface StatisticsInfoService {

    /**
     * 根据条件查询列表
     */
    List<StatisticsInfo> findListByParam(StatisticsInfoQuery param);

    /**
     * 根据条件查询列表
     */
    Integer findCountByParam(StatisticsInfoQuery param);

    /**
     * 分页查询
     */
    PaginationResultVO<StatisticsInfo> findListByPage(StatisticsInfoQuery param);

    /**
     * 新增
     */
    Integer add(StatisticsInfo bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<StatisticsInfo> listBean);

    /**
     * 批量新增/修改
     */
    Integer addOrUpdateBatch(List<StatisticsInfo> listBean);

    /**
     * 多条件更新
     */
    Integer updateByParam(StatisticsInfo bean, StatisticsInfoQuery param);

    /**
     * 多条件删除
     */
    Integer deleteByParam(StatisticsInfoQuery param);

    /**
     * 根据StatisticsDateAndDataType查询对象
     */
    StatisticsInfo getStatisticsInfoByStatisticsDateAndDataType(String statisticsDate, Integer dataType);


    /**
     * 根据StatisticsDateAndDataType修改
     */
    Integer updateStatisticsInfoByStatisticsDateAndDataType(StatisticsInfo bean, String statisticsDate, Integer dataType);


    /**
     * 根据StatisticsDateAndDataType删除
     */
    Integer deleteStatisticsInfoByStatisticsDateAndDataType(String statisticsDate, Integer dataType);

    void statisticsData(String date);

    List<StatisticsDataVO> loadWeeklyStatisticsData();
}