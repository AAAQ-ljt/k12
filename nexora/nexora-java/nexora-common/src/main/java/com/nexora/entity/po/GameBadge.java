package com.nexora.entity.po;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 徽章定义：声明式规则（rule_type + rule_value），解锁判定由 PointAwardComponent 统一执行。
 */
@Data
public class GameBadge implements Serializable {

    private String badgeId;

    private String name;

    /** 解锁条件文案（学生端展示） */
    private String description;

    /** 图标（emoji 或资源ID） */
    private String icon;

    /** 可见学段，多学段逗号分隔，NULL = 全学段 */
    private String stageScope;

    /** 规则：FIRST_PASS / STREAK / TOTAL_POINTS / MASTERY_COUNT / CODING_COUNT / COMBO_MAX / CREATION */
    private String ruleType;

    /** 阈值 */
    private Integer ruleValue;

    /** 解锁奖励积分 */
    private Integer rewardPoints;

    private Integer sort;

    /** 0停用 1启用 */
    private Integer status;

    private Date createTime;

    private Date updateTime;
}
