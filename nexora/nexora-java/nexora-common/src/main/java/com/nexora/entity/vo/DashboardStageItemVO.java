package com.nexora.entity.vo;

/**
 * 控制面板：学段用户分布项
 */
public class DashboardStageItemVO {

    /** 学段编码 PRIMARY_LOW/PRIMARY_HIGH/JUNIOR/SENIOR */
    private String stage;

    /** 学段学生数 */
    private Long count;

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}