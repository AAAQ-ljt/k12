package com.nexora.entity.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 徽章墙单行（二期·积分游戏化 A-4）。
 *
 * 进度口径与解锁判定共用 {@code PointAwardComponent.ruleReached} 同一套规则，
 * 避免出现「进度满了但没发徽章」的展示与发放不一致。
 */
@Data
public class PointBadgeVO implements Serializable {

    /** 徽章 ID */
    private String badgeId;

    /** 徽章名 */
    private String name;

    /** 获得条件描述 */
    private String description;

    /** 图标（emoji） */
    private String icon;

    /** 规则类型：FIRST_PASS/STREAK/COMBO_MAX/MASTERY_COUNT/TOTAL_POINTS/CODING_COUNT/CREATION */
    private String ruleType;

    /** 规则阈值 */
    private Integer ruleValue;

    /** 解锁奖励积分 */
    private Integer rewardPoints;

    /** 是否已解锁 */
    private Boolean unlocked;

    /** 解锁时间（未解锁为 null） */
    private Date unlockedTime;

    /** 当前进度值（与 ruleValue 同口径） */
    private Integer progress;

    /** 进度文案，如「3/7 天」「120/1000 分」 */
    private String progressText;
}
