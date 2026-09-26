package com.nexora.component;

import com.nexora.mappers.AiUsageRecordMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * AI 消耗统计写入组件：所有消耗统一落 ai_usage_record（按天 + 类型 + 模型累计）。
 *
 * 设计取舍：
 * - **只增不查**：写入方是 Advisor（对话类）与 ImageProviderRouter（生图类），读取方是工作台聚合 SQL；
 * - **统计失败不阻断业务**：AI 调用已经成功返回，统计写库异常只告警，不影响用户看到的结果；
 * - **同请求多次调用分别累计**：意图路由与回复生成是两次独立的大模型调用，各自计一次。
 */
@Slf4j
@Component
public class AiUsageRecordComponent {

    /** 消耗类型：大模型调用（对话、意图路由、绘本文案、动画脚本、学习路径、出题、文档整理等） */
    public static final String TYPE_CHAT = "CHAT";

    /** 消耗类型：文生图调用（学生绘本插图 / 单页补画 / 管理端生图测试），按张计次 */
    public static final String TYPE_IMAGE = "IMAGE";

    /** 消耗类型：TTS 语音合成（学生绘本旁白 / 单页补录 / 管理端语音测试），按次计 */
    public static final String TYPE_TTS = "TTS";

    @Resource
    private AiUsageRecordMapper aiUsageRecordMapper;

    /**
     * 记录一次大模型调用的 token 消耗。
     * 部分流式响应不带 usage，两次用量都为空时直接跳过，避免产生「0 token 调用」污染统计。
     */
    public void recordChatUsage(String model, Integer promptTokens, Integer completionTokens) {
        if (isZero(promptTokens) && isZero(completionTokens)) {
            return;
        }
        write(TYPE_CHAT, model, 1, promptTokens, completionTokens);
    }

    /** 记录一次成功的文生图调用（按张计次，不产生 token） */
    public void recordImageUsage(String provider) {
        write(TYPE_IMAGE, provider, 1, null, null);
    }

    /** 记录一次成功的语音合成调用（按次计，不产生 token） */
    public void recordTtsUsage(String model) {
        write(TYPE_TTS, model, 1, null, null);
    }

    private void write(String usageType, String model, long callCount, Integer promptTokens, Integer completionTokens) {
        try {
            aiUsageRecordMapper.upsertUsage(usageType, model == null ? "" : model, callCount,
                    nz(promptTokens), nz(completionTokens));
        } catch (Exception e) {
            log.warn("AI 消耗统计写入失败：type={}, model={}", usageType, model, e);
        }
    }

    private boolean isZero(Integer value) {
        return value == null || value == 0;
    }

    private long nz(Integer value) {
        return value == null ? 0L : value.longValue();
    }
}
