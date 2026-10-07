package com.nexora.mappers;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 练习作答的连击派生查询（二期 COMBO，手写精简 mapper）。
 *
 * 连击不落库、不加字段：直接从 practice_record 里最近 N 条作答的对错派生，
 * 避免「计数器与实际作答不一致」这类只有长期使用才会暴露的漂移。
 */
public interface PracticeComboMapper {

    /** 最近的作答对错（1=对 0=错），按时间倒序 */
    List<Integer> selectRecentIsCorrect(@Param("userId") String userId, @Param("limit") int limit);
}
