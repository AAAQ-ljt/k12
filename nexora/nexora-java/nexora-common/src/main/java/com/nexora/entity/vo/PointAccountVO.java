package com.nexora.entity.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 学生积分账户概览（学生端「成长中心」/卡片展示用）。
 *
 * 学段化命名（小低叫星星、初高叫积分+段位）由前端按学段决定，接口只给中性数值，
 * 避免把展示口径写进数据层（见二期规划 §10 决策 1 待拍板）。
 */
@Data
public class PointAccountVO implements Serializable {

    /** 累计获得积分（只增不减） */
    private Integer totalPoints;

    /** 可用积分（兑换后减少） */
    private Integer availablePoints;

    /** 当前等级 */
    private Integer level;

    /** 段位名（初高中段：青铜→王者；小学段为空，前端按「N 颗星」展示） */
    private String levelName;

    /** 本级起点积分（进度条左端） */
    private Integer levelFloor;

    /** 升到下一级还需的积分；已满级为 0 */
    private Integer nextLevelPoints;

    /** 连续学习天数 */
    private Integer streakDays;

    /** 今日已获积分（只统计受每日上限约束的可重复来源） */
    private Integer todayPoints;

    /** 每日积分上限 */
    private Integer dailyCap;
}
