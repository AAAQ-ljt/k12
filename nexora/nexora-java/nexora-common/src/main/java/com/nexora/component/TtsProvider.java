package com.nexora.component;

import java.util.Set;

/**
 * TTS 语音合成供应商统一入口（当前仅 MiMo 实现，接口化便于后续扩展）。
 * 本接口只负责「文本 → 音频字节」；产物落盘与入库由调用方完成（与绘本插图同构）。
 */
public interface TtsProvider {

    /**
     * MiMo 预置音色白名单（mimo-v2.5-tts）：mimo_default + 中文四音色 + 英文四音色。
     * 任何调用传入的 voice 必须命中该集合，避免任意字符串透传到上游接口。
     */
    Set<String> ALLOWED_VOICES = Set.of(
            "mimo_default", "冰糖", "茉莉", "苏打", "白桦",
            "Mia", "Chloe", "Milo", "Dean");

    static boolean isValidVoice(String voice) {
        return voice != null && ALLOWED_VOICES.contains(voice);
    }

    /**
     * 合成语音：文本 → 音频字节（mp3）；失败返回用户可读原因。
     */
    TtsSynthesizeResult synthesize(TtsSynthesizeRequest request);

    /**
     * 音色解析：显式指定优先（须命中白名单），其次按学段取默认，最后回落全局默认。
     */
    String resolveVoice(String stage, String voice);

    /**
     * 供应商是否已配置可用（API Key 缺失/占位时调用方应跳过语音链路，不阻断主流程）。
     */
    boolean isConfigured();

    /**
     * 模型标识（用量统计按此落库，如 mimo-v2.5-tts）。
     */
    String modelId();

    /**
     * 音频容器格式（如 mp3），前端转 Blob 时作为 MIME 依据。
     */
    String audioFormat();

    /**
     * 该供应商推荐的最大并发数。
     */
    default int maxConcurrency() {
        return 1;
    }
}
