package com.nexora.mappers;

import com.nexora.entity.po.UserPromptRule;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户端提示词规则（计划 C2，手写精简 mapper）。
 */
public interface UserPromptRuleMapper {

    /** 新增规则，返回自增 ID */
    Integer insert(UserPromptRule rule);

    /** 我的规则（含停用，按类型与时间排序；statusList 为空表示不限状态） */
    List<UserPromptRule> selectByUser(@Param("userId") String userId,
                                      @Param("statusList") List<Integer> statusList);

    /** 启用的规则条数（限条判断） */
    Integer countEnabled(@Param("userId") String userId);

    /** 停用/启用某条规则（只能操作自己的） */
    Integer updateStatus(@Param("userId") String userId,
                         @Param("ruleId") Long ruleId,
                         @Param("status") int status);

    /** 物理删除某条规则（只能操作自己的） */
    Integer deleteById(@Param("userId") String userId, @Param("ruleId") Long ruleId);

    /** 清空某来源的规则（C3「重置偏好」用：清掉学生自填，保留系统默认） */
    Integer deleteBySource(@Param("userId") String userId, @Param("source") String source);
}
