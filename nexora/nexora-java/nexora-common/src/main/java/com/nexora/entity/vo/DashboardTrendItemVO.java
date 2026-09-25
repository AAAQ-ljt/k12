package com.nexora.entity.vo;

/**
 * 控制面板：近 7 天对话趋势项
 */
public class DashboardTrendItemVO {

    /** 日期标签 MM-dd */
    private String dayLabel;

    /** 当日对话条数 */
    private Long count;

    public String getDayLabel() {
        return dayLabel;
    }

    public void setDayLabel(String dayLabel) {
        this.dayLabel = dayLabel;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}