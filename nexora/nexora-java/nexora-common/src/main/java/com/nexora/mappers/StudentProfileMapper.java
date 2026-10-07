package com.nexora.mappers;

import com.nexora.entity.po.StudentProfile;
import org.apache.ibatis.annotations.Param;

/**
 * 学生画像（计划 C1，手写精简 mapper）。
 */
public interface StudentProfileMapper {

    /** 按学生取画像（不存在返回 null） */
    StudentProfile selectByUserId(@Param("userId") String userId);

    /** 覆盖写画像摘要（不存在则插入）；dirty 置 0，refresh_time 置当前时间 */
    Integer upsertSummary(StudentProfile profile);

    /** 标记待刷新（学习事件发生后调用，避免每次事件都重建画像） */
    Integer markDirty(@Param("userId") String userId);

    /** 更新《我的学习偏好》页锚点（计划 C3） */
    Integer updatePreferenceDocId(@Param("userId") String userId, @Param("preferenceDocId") String preferenceDocId);
}
