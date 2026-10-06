package com.nexora.entity.po;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 学生积分账户（一人一行）：等级 / 连续天数 / 排行榜的数据源。
 * 口径见 docs/二期规划设计-20261006.md §3 A-1。
 */
@Data
public class StudentPointAccount implements Serializable {

    private String userId;

    /** 学段【冗余快照：本学段榜免 join】 */
    private String stage;

    /** 累计获得积分（只增不减，等级与成就依据） */
    private Integer totalPoints;

    /** 可用积分（兑换扣减；二期先与 total 同值） */
    private Integer availablePoints;

    /** 当前等级（由 totalPoints 按 LEVEL_STEP 阶梯折算） */
    private Integer level;

    /** 连续学习天数 */
    private Integer streakDays;

    /** 连续天数最近归属日期（判断断签） */
    private Date lastStreakDate;

    private Date createTime;

    private Date updateTime;
}
