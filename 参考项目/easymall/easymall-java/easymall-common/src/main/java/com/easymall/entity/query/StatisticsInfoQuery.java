package com.easymall.entity.query;

import java.math.BigDecimal;


/**
 * 数据统计结果参数
 */
public class StatisticsInfoQuery extends BaseParam {


    /**
     * 日期
     */
    private String statisticsDate;

    private String statisticsDateFuzzy;

    /**
     * 数据类型
     */
    private Integer dataType;

    /**
     * 统计数据
     */
    private BigDecimal dataValue;

    private String statisticsDateStart;

    private String statisticsDateEnd;

    public String getStatisticsDateStart() {
        return statisticsDateStart;
    }

    public void setStatisticsDateStart(String statisticsDateStart) {
        this.statisticsDateStart = statisticsDateStart;
    }

    public String getStatisticsDateEnd() {
        return statisticsDateEnd;
    }

    public void setStatisticsDateEnd(String statisticsDateEnd) {
        this.statisticsDateEnd = statisticsDateEnd;
    }

    public void setStatisticsDate(String statisticsDate) {
        this.statisticsDate = statisticsDate;
    }

    public String getStatisticsDate() {
        return this.statisticsDate;
    }

    public void setStatisticsDateFuzzy(String statisticsDateFuzzy) {
        this.statisticsDateFuzzy = statisticsDateFuzzy;
    }

    public String getStatisticsDateFuzzy() {
        return this.statisticsDateFuzzy;
    }

    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

    public Integer getDataType() {
        return this.dataType;
    }

    public void setDataValue(BigDecimal dataValue) {
        this.dataValue = dataValue;
    }

    public BigDecimal getDataValue() {
        return this.dataValue;
    }

}
