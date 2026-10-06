package com.nexora.entity.query;

import lombok.Data;

/**
 * 徽章定义查询条件（管理端与徽章墙复用）。
 */
@Data
public class GameBadgeQuery {

    private String badgeId;

    /** 0停用 1启用；空=不限 */
    private Integer status;

    /** 学段可见性过滤（命中 stage_scope 为本学段或全学段） */
    private String stage;
}
