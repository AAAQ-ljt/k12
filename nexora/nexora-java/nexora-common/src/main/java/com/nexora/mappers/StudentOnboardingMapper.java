package com.nexora.mappers;

import com.nexora.entity.po.StudentOnboarding;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 新手引导进度（计划 D1，手写精简 mapper）。
 */
public interface StudentOnboardingMapper {

    /** 取进度（不存在返回 null） */
    StudentOnboarding selectByUserId(@Param("userId") String userId);

    /** 首建进度行（已存在则忽略） */
    Integer insertIfAbsent(StudentOnboarding record);

    /** 更新已完成的步骤列表（JSON 字符串） */
    Integer updateSteps(@Param("userId") String userId,
                        @Param("stepsDone") String stepsDone,
                        @Param("version") Integer version,
                        @Param("finished") boolean finished);

    /** 标记看过欢迎卡 / 跳过 */
    Integer markWelcome(@Param("userId") String userId,
                        @Param("welcomeSeen") int welcomeSeen,
                        @Param("skipped") int skipped,
                        @Param("stageSnapshot") String stageSnapshot);

    /** 记录打开引导中心的时间 */
    Integer touchOpen(@Param("userId") String userId);

    /** 重看：清空已完成步骤与完成时间（保留"看过欢迎卡"与版本） */
    Integer resetSteps(@Param("userId") String userId);

    /** 批量取（管理端/统计用，可空） */
    List<StudentOnboarding> selectByUserIds(@Param("userIds") List<String> userIds);
}
