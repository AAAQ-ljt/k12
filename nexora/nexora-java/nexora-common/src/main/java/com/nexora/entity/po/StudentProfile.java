package com.nexora.entity.po;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 学生画像（计划 C1）：对话时注入"画像速览"，让 AI 不调工具也知道学生学习概况。
 *
 * 快照式：由学习事件标记 dirty，下次对话前重建；仅缓存摘要文本，细节一律回源查询（掌握度/路径/流水）。
 */
@Data
public class StudentProfile implements Serializable {

    /** 学生 ID */
    private String userId;

    /** 学段快照 */
    private String stage;

    /** 年级快照 */
    private String grade;

    /** 掌握度摘要（数量分布 + 平均分 + 最弱 3 个知识点） */
    private String masterySummary;

    /** 路径摘要（进行中路径数、当前路径与节点、进度） */
    private String pathSummary;

    /** 薄弱知识点清单（掌握度 < 70 且有练习记录，最多 5 条） */
    private String weakPoints;

    /** 积分摘要（累计/可用/连续天数/等级称谓） */
    private String pointSummary;

    /** 《我的学习偏好》知识页 ID（计划 C3 的会话锚点） */
    private String preferenceDocId;

    /** 待刷新标记：1=学习事件已发生，下次对话前重建 */
    private Integer dirty;

    /** 最近一次重建时间 */
    private Date refreshTime;

    private Date createTime;

    private Date updateTime;
}
