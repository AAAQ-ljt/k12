package com.nexora.mappers;

import com.nexora.entity.po.GameBadge;
import com.nexora.entity.query.GameBadgeQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 徽章定义（手写精简 mapper）。
 */
public interface GameBadgeMapper {

    /** 启用中的徽章（可按学段过滤：stage_scope 为空=全学段） */
    List<GameBadge> selectListByParam(GameBadgeQuery query);

    GameBadge selectByBadgeId(@Param("badgeId") String badgeId);
}
