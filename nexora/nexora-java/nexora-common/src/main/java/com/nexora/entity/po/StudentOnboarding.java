package com.nexora.entity.po;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 新手引导进度（计划 D1）：记"看没看过、看到第几步、看的是哪一版"。
 */
@Data
public class StudentOnboarding implements Serializable {

    /** 学生 ID */
    private String userId;

    /** 首次引导时的学段快照 */
    private String stageSnapshot;

    /** 已看完的引导版本号 */
    private Integer version;

    /** 是否看过欢迎卡 */
    private Integer welcomeSeen;

    /** 是否选择过"我先自己看看" */
    private Integer skipped;

    /** 已完成步骤（JSON 数组） */
    private String stepsDone;

    /** 最近一次走完导览的时间 */
    private Date finishedTime;

    /** 最近一次打开引导中心的时间 */
    private Date lastOpenTime;

    private Date createTime;

    private Date updateTime;
}
