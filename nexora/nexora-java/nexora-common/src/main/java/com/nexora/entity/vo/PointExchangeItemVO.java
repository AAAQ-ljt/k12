package com.nexora.entity.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 兑换商品（学生端兑换页单行，二期 A-7）。
 */
@Data
public class PointExchangeItemVO implements Serializable {

    /** 商品码：UPLOAD_QUOTA / VOICE_<音色码> */
    private String itemCode;

    /** 商品名，如「上传额度 +200MB」「朗读音色 · 茉莉」 */
    private String itemName;

    /** 商品说明（效果 + 限制，学生端展示） */
    private String effect;

    /** 花费可用积分 */
    private Integer costPoints;

    /** 最多可兑换次数（0=不限次） */
    private Integer maxTimes;

    /** 我已兑换次数 */
    private Integer usedTimes;

    /** 我还能兑换几次（不限次时为 -1） */
    private Integer remainingTimes;

    /** 我的可用积分是否够（前端可直接置灰按钮） */
    private Boolean affordable;

    /** 是否已拥有/已解锁（已解锁的商品不再展示兑换按钮） */
    private Boolean unlocked;

    /** 分类：UPLOAD_QUOTA 容量类 / VOICE 音色类 */
    private String category;
}
