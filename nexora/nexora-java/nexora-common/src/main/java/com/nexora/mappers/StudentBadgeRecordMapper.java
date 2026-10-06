package com.nexora.mappers;

import com.nexora.entity.po.StudentBadgeRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Date;

/**
 * 学生徽章解锁记录（手写精简 mapper）；insertIgnore 返回 0 表示已解锁过。
 */
public interface StudentBadgeRecordMapper {

    Integer insertIgnore(StudentBadgeRecord record);

    /** 已解锁的徽章 ID 列表 */
    List<String> selectBadgeIdsByUser(@Param("userId") String userId);

    /** 解锁数量（徽章规则 MASTERY_COUNT 等计数用；按 badge 前缀可选过滤） */
    Integer countByUserAndTimeRange(@Param("userId") String userId,
                                    @Param("start") Date start,
                                    @Param("end") Date end);

    /** 徽章墙用：解锁记录（含解锁时间） */
    List<StudentBadgeRecord> selectByUser(@Param("userId") String userId);
}
