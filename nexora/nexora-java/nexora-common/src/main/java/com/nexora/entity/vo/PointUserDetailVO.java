package com.nexora.entity.vo;

import com.nexora.entity.po.StudentPointRecord;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理端学生成长详情（二期 A-9）：学习档案抽屉里展示的积分/徽章概览 + 最近流水。
 */
@Data
public class PointUserDetailVO implements Serializable {

    /** 学生 ID */
    private String userId;

    /** 学段 */
    private String stage;

    /** 当前等级 */
    private Integer level;

    /** 段位名（小学段为空，按「N 颗星」展示） */
    private String levelName;

    /** 累计获得积分（只增不减） */
    private Integer totalPoints;

    /** 可用积分（兑换后减少） */
    private Integer availablePoints;

    /** 连续学习天数 */
    private Integer streakDays;

    /** 已解锁徽章数 */
    private Integer unlockedBadgeCount;

    /** 该学段可见徽章总数 */
    private Integer badgeTotal;

    /** 最近 10 条流水（时间倒序） */
    private List<StudentPointRecord> recentRecords = new ArrayList<>();
}
