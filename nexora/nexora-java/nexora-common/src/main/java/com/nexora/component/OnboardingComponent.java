package com.nexora.component;

import com.alibaba.fastjson2.JSON;
import com.nexora.entity.po.StudentOnboarding;
import com.nexora.entity.vo.OnboardingStatusVO;
import com.nexora.mappers.StudentOnboardingMapper;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 新手引导进度（计划 D1）。
 *
 * 职责边界：后端只管"看没看过、看到第几步、是哪一版"，**步骤文案与高亮目标放前端按学段配置**，
 * 这样平台改文案不用发版后端；引导失败或接口异常时前端静默降级（不显示引导），
 * 所以这里的异常一律吞掉并记日志，绝不因为引导影响正常使用。
 */
@Slf4j
@Component
public class OnboardingComponent {

    /** 引导脚本版本：前端加了新步骤就 +1，老用户下次进引导中心会看到「导览有更新」 */
    public static final int SCRIPT_VERSION = 1;

    @Resource
    private StudentOnboardingMapper onboardingMapper;

    /** 取状态（不存在则按"第一次"返回，不建行——首建行发生在记录欢迎卡或步骤时） */
    public OnboardingStatusVO status(String userId) {
        OnboardingStatusVO vo = new OnboardingStatusVO();
        vo.setScriptVersion(SCRIPT_VERSION);
        if (StringTools.isEmpty(userId)) {
            vo.setFirstTime(true);
            return vo;
        }
        try {
            StudentOnboarding record = onboardingMapper.selectByUserId(userId);
            if (record == null) {
                vo.setFirstTime(true);
                vo.setWelcomeSeen(false);
                vo.setSkipped(false);
                vo.setVersion(0);
                vo.setNeedUpdate(true);
                return vo;
            }
            boolean welcomeSeen = record.getWelcomeSeen() != null && record.getWelcomeSeen() == 1;
            boolean skipped = record.getSkipped() != null && record.getSkipped() == 1;
            int version = record.getVersion() == null ? 0 : record.getVersion();
            vo.setWelcomeSeen(welcomeSeen);
            vo.setSkipped(skipped);
            vo.setVersion(version);
            vo.setFirstTime(!welcomeSeen && !skipped);
            vo.setNeedUpdate(version < SCRIPT_VERSION);
            vo.setStepsDone(parseSteps(record.getStepsDone()));
            return vo;
        } catch (Exception e) {
            log.warn("读取新手引导状态失败（按首次处理）userId={}", userId, e);
            vo.setFirstTime(false);
            return vo;
        }
    }

    /** 记"看过欢迎卡 / 选择先自己看看"（首次调用会建行） */
    public void recordWelcome(String userId, String stage, boolean welcomeSeen, boolean skipped) {
        if (StringTools.isEmpty(userId)) {
            return;
        }
        try {
            ensureRow(userId, stage);
            onboardingMapper.markWelcome(userId, welcomeSeen ? 1 : 0, skipped ? 1 : 0,
                    StringTools.isEmpty(stage) ? "" : stage);
        } catch (Exception e) {
            log.warn("记录欢迎卡状态失败 userId={}", userId, e);
        }
    }

    /** 批量上报进度（前端在步骤完成或退出时上报一次，避免每步一次请求） */
    public void recordSteps(String userId, String stage, List<String> steps, boolean finished) {
        if (StringTools.isEmpty(userId)) {
            return;
        }
        try {
            ensureRow(userId, stage);
            List<String> safeSteps = steps == null ? new ArrayList<>() : steps;
            onboardingMapper.updateSteps(userId, JSON.toJSONString(safeSteps),
                    finished ? SCRIPT_VERSION : null, finished);
        } catch (Exception e) {
            log.warn("记录引导步骤失败 userId={}", userId, e);
        }
    }

    /** 重看：清空已完成步骤（欢迎卡与版本保留，学生可随时从引导中心再走一遍） */
    public void reset(String userId, String stage) {
        if (StringTools.isEmpty(userId)) {
            return;
        }
        try {
            ensureRow(userId, stage);
            onboardingMapper.resetSteps(userId);
        } catch (Exception e) {
            log.warn("重置引导进度失败 userId={}", userId, e);
        }
    }

    /** 打开引导中心：只更新时间戳 */
    public void touchOpen(String userId, String stage) {
        if (StringTools.isEmpty(userId)) {
            return;
        }
        try {
            ensureRow(userId, stage);
            onboardingMapper.touchOpen(userId);
        } catch (Exception e) {
            log.warn("记录引导中心打开时间失败 userId={}", userId, e);
        }
    }

    private void ensureRow(String userId, String stage) {
        StudentOnboarding record = new StudentOnboarding();
        record.setUserId(userId);
        record.setStageSnapshot(StringTools.isEmpty(stage) ? "" : stage);
        onboardingMapper.insertIfAbsent(record);
    }

    private List<String> parseSteps(String stepsJson) {
        if (StringTools.isEmpty(stepsJson)) {
            return new ArrayList<>();
        }
        try {
            List<String> steps = JSON.parseArray(stepsJson, String.class);
            return steps == null ? new ArrayList<>() : steps;
        } catch (Exception e) {
            log.warn("引导步骤 JSON 解析失败，按空处理：{}", stepsJson);
            return new ArrayList<>();
        }
    }
}
