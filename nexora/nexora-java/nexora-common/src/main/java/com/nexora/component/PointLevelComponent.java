package com.nexora.component;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 积分等级折算（纯计算，不落库；写入统一走 {@link PointAwardComponent}）。
 *
 * 等级阶梯来自 system_config 的 GAME.LEVEL_STEP（逗号分隔的累计积分门槛，1 级起点固定为 0），
 * 运营可在管理端「系统设置」里调整；配置缺失或非法时回落代码默认值。
 * 口径见 docs/二期规划设计-20261006.md §3 A-3。
 */
@Slf4j
@Component
public class PointLevelComponent {

    /** GAME 组下的等级阶梯键 */
    public static final String KEY_LEVEL_STEP = "LEVEL_STEP";

    /** 默认阶梯：1-10 级的累计积分门槛（10 级之后封顶） */
    private static final int[] DEFAULT_THRESHOLDS = {0, 100, 300, 600, 1000, 1500, 2100, 2800, 3600, 4500};

    @Resource
    private SystemConfigComponent systemConfigComponent;

    /** 按累计积分折算等级（1 起；超过最高门槛时封顶最高级） */
    public int levelOf(int totalPoints) {
        int[] steps = thresholds();
        int level = 1;
        for (int i = 1; i < steps.length; i++) {
            if (totalPoints >= steps[i]) {
                level = i + 1;
            } else {
                break;
            }
        }
        return level;
    }

    /** 升到下一级还需要的积分；已满级返回 0 */
    public int pointsToNextLevel(int totalPoints) {
        for (int step : thresholds()) {
            if (totalPoints < step) {
                return step - totalPoints;
            }
        }
        return 0;
    }

    /** 当前等级的门槛（前端进度条起点）；越界返回 0 */
    public int levelFloor(int level) {
        int[] steps = thresholds();
        if (level <= 1) {
            return 0;
        }
        return level - 1 < steps.length ? steps[level - 1] : steps[steps.length - 1];
    }

    /** 解析阶梯配置：逗号分隔的累计积分；缺省/非法（不足 2 档或非递增）时回落默认值 */
    private int[] thresholds() {
        String raw = systemConfigComponent.getValue(SystemConfigComponent.GROUP_GAME, KEY_LEVEL_STEP, null);
        if (raw == null || raw.isBlank()) {
            return DEFAULT_THRESHOLDS;
        }
        try {
            String[] parts = raw.trim().split(",");
            int[] parsed = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                parsed[i] = Integer.parseInt(parts[i].trim());
            }
            if (parsed.length < 2 || parsed[0] != 0) {
                return DEFAULT_THRESHOLDS;
            }
            for (int i = 1; i < parsed.length; i++) {
                if (parsed[i] <= parsed[i - 1]) {
                    log.warn("GAME.LEVEL_STEP 非递增，回落默认阶梯：{}", raw);
                    return DEFAULT_THRESHOLDS;
                }
            }
            return parsed;
        } catch (NumberFormatException e) {
            log.warn("GAME.LEVEL_STEP 解析失败，回落默认阶梯：{}", raw);
            return DEFAULT_THRESHOLDS;
        }
    }
}
