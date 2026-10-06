package com.nexora.entity.po;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 学生积分流水。唯一索引 uk_user_biz(user_id, biz_type, biz_id) 是「防重复发分」的核心。
 */
@Data
public class StudentPointRecord implements Serializable {

    private Long recordId;

    private String userId;

    /** 学段【冗余快照】 */
    private String stage;

    /** 来源：SIGN_IN / LESSON_QUIZ / PATH_TEST / CODING_PROBLEM / PICTURE_BOOK / ANIMATION / WIKI_CONFIRM / MASTERY / BADGE / STREAK / COMBO / EXCHANGE / ADMIN_ADJUST */
    private String bizType;

    /** 业务幂等键（课时ID / 题目ID / 知识点ID / yyyyMMdd 等） */
    private String bizId;

    /** 积分变动（正=获得 负=消耗） */
    private Integer points;

    /** 变动后累计积分 */
    private Integer balanceAfter;

    /** 展示文案，如「通关《冒泡排序》」 */
    private String reason;

    private Date createTime;
}
