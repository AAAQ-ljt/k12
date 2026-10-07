package com.nexora.entity.vo;

import com.nexora.entity.po.UserPromptRule;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 我的偏好设置页数据（计划 C2）：规则列表 + 可选类型 + 生效状态。
 */
@Data
public class UserPromptRuleListVO implements Serializable {

    /** 总开关是否开启（关闭时前端显示"该功能暂未开放"） */
    private Boolean enabled;

    /** 最多可启用条数 */
    private Integer maxCount;

    /** 已启用条数 */
    private Integer enabledCount;

    /** 我的规则（含停用） */
    private List<UserPromptRule> rules = new ArrayList<>();

    /** 可选的规则类型 */
    private List<PromptRuleTypeVO> types = new ArrayList<>();
}
