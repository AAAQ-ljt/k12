package com.nexora.mappers;

import com.nexora.entity.po.StudentPointAccount;
import com.nexora.entity.vo.PointRankItemVO;
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
     * 排行榜取样（A-5）：startTime 为空取累计积分榜，非空取该时间点之后的周榜（流水求和）。
     * 一次联表把昵称/等级带出，避免逐行再查（禁止循环里访问数据库）。
     * 注意：编程比赛成绩不进全局积分，排行榜只统计积分流水累计值。
     */
    List<PointRankItemVO> selectRankRows(@Param("stage") String stage,
                                         @Param("startTime") Date startTime,
                                         @Param("limit") int limit);

    /** 累计积分严格高于我的学生数（+1 即我的名次） */
    Integer countAboveTotal(@Param("stage") String stage, @Param("points") int points);
}
