package com.nexora.entity.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 积分排行榜查询结果（二期·积分游戏化 A-5）。
 *
 * 学段口径（A-10）：小学低/高年级只展示自己的星星与徽章，不开放排行榜（避免低龄排名压力），
 * 此时 enabled=false 且 list 为空，前端据此隐藏榜单入口。
 */
@Data
public class PointRankResultVO implements Serializable {

    /** 榜单类型：total 累计榜 / week 周榜 */
    private String type;

    /** 榜单范围：stage 本学段（当前固定本学段，不做全站混排） */
    private String scope;

    /** 是否开放排行 */
    private Boolean enabled;

    /** 榜单（最多 20 行，已脱敏） */
    private List<PointRankItemVO> list = new ArrayList<>();

    /** 我的名次（未上榜为 null） */
    private Integer myRank;

    /** 我的积分（与榜单同口径：总榜为累计、周榜为本周） */
    private Integer myPoints;

    /** 不开放时的说明文案 */
    private String tip;
}
