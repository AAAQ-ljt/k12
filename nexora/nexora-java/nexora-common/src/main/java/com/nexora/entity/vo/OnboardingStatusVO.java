package com.nexora.entity.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 新手引导状态（计划 D1）：首次是否要自动弹、看到第几步、当前脚本版本。
 */
@Data
public class OnboardingStatusVO implements Serializable {

    /** 是否第一次（从未看过欢迎卡且没跳过）→ 前端据此自动弹欢迎卡 */
    private Boolean firstTime;

    /** 是否看过欢迎卡 */
    private Boolean welcomeSeen;

    /** 是否选择过「我先自己看看」 */
    private Boolean skipped;

    /** 已看完的引导版本 */
    private Integer version;

    /** 当前引导脚本版本（前端配置下发，后端只做比对） */
    private Integer scriptVersion;

    /** 是否有更新（已看版本 < 当前版本）→ 引导中心提示「导览有更新」 */
    private Boolean needUpdate;

    /** 已完成的步骤 key 列表（中途退出可续播） */
    private List<String> stepsDone = new ArrayList<>();
}
