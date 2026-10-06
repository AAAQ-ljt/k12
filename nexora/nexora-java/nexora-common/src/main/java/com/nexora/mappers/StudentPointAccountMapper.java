package com.nexora.mappers;

import com.nexora.entity.po.StudentPointAccount;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 积分账户（手写精简 mapper：只声明积分体系真正用到的语句，
 * 不走代码生成器的全量 CRUD 模板——与 LearningAnalysisMapper / DashboardMapper 同一取舍）。
 */
public interface StudentPointAccountMapper {

    /** 按学生取账户（不存在返回 null） */
    StudentPointAccount selectByUserId(@Param("userId") String userId);

    /**
     * 原子加/减分并返回受影响行数：账户不存在时插入，存在时累加。
     * 只改积分与学段快照，等级由调用方按新余额另行计算更新。
     */
    Integer upsertIncrease(@Param("userId") String userId,
                           @Param("stage") String stage,
                           @Param("points") int points);

    /** 更新等级（仅当与现值不同时） */
    Integer updateLevel(@Param("userId") String userId, @Param("level") int level);

    /** 更新连续学习天数与归属日期 */
    Integer updateStreak(@Param("userId") String userId,
                         @Param("streakDays") int streakDays,
                         @Param("lastStreakDate") Date lastStreakDate);

    /**
     * 本学段累计积分榜（A-5 排行榜；limit 由调用方给上限）
     * 注意：编程比赛成绩不进全局积分，排行榜只统计积分流水累计值。
     */
    List<StudentPointAccount> selectTopByStage(@Param("stage") String stage, @Param("limit") int limit);

    /** 全站累计积分榜 */
    List<StudentPointAccount> selectTopAll(@Param("limit") int limit);
}
