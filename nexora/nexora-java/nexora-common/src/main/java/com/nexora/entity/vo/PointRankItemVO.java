package com.nexora.entity.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 积分排行榜单行（二期·积分游戏化 A-5）。
 *
 * nickName 是 mapper 联表查出的原始昵称，只在业务层用于生成 displayName；
 * 外发一律用 displayName（首字符 + *** + ID 后 4 位），避免学生端通过接口拿到真实昵称。
 */
@Data
public class PointRankItemVO implements Serializable {

    /** 名次（从 1 开始） */
    private Integer rankNo;

    /** 学生 ID（不外发，仅业务层比对是否为本人） */
    private String userId;

    /** 原始昵称（不外发） */
    private String nickName;

    /** 脱敏展示名（外发） */
    private String displayName;

    /** 学段 */
    private String stage;

    /** 等级 */
    private Integer level;

    /** 段位名（初高中段；小学段为空，前端按「N 颗星」展示） */
    private String levelName;

    /** 榜内积分（周榜为本周一至今净得，总榜为累计积分） */
    private Integer points;

    /** 是否本人 */
    private Boolean me;
}
