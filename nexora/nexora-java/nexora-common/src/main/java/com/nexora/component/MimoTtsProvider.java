package com.nexora.component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.nexora.entity.enums.StageEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;

/**
 * MiMo TTS 语音合成实现：小米 MiMo OpenAI 兼容接口 /v1/chat/completions，
 * 音频以 base64 返回在 choices[0].message.audio.data（接口细节见 docs/mimo-v2.5-tts接口文档.md）。
 * 上游协议复用 Chat 消息结构：user 消息承载语气/风格指令，assistant 消息承载待合成文本。
 * 未流式（mp3 整包返回）：产物走落盘回放场景，无需流式首声优化。
 */
@Slf4j
@Component
public class MimoTtsProvider implements TtsProvider {

    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_BACKOFF_MS = 1500;
    private static final int CONNECT_TIMEOUT_SECONDS = 10;

    @Value("${project.ai.tts.model:mimo-v2.5-tts}")
    private String ttsModel;

    @Value("${project.ai.tts.base-url:https://api.xiaomimimo.com/v1}")
    private String ttsBaseUrl;

    @Value("${project.ai.tts.path:/chat/completions}")
    private String ttsPath;

    @Value("${project.ai.tts.api-key:}")
    private String ttsApiKey;

    /** 音频容器格式：mp3（比 wav 小约 6 倍，浏览器 audio 原生可播） */
    @Value("${project.ai.tts.format:mp3}")
    private String ttsFormat;

    @Value("${project.ai.tts.timeout-seconds:120}")
    private int timeoutSeconds;

    /** 学段默认音色：小低/小高各配一个，其余学段回落 voice-default（绘本仅小学阶段） */
    @Value("${project.ai.tts.voice-primary-low:冰糖}")
    private String voicePrimaryLow;

    @Value("${project.ai.tts.voice-primary-high:白桦}")
    private String voicePrimaryHigh;

    @Value("${project.ai.tts.voice-default:mimo_default}")
    private String voiceDefault;

    /** 语气/风格指令（user 消息），配置为空则不传 user 消息 */
    @Value("${project.ai.tts.tone:}")
    private String defaultTone;

    @Override
    public int maxConcurrency() {
        return 2;
    }

    @Override
    public boolean isConfigured() {
        return ttsApiKey != null && !ttsApiKey.isBlank() && !ttsApiKey.startsWith("sk-xxx");
    }

    @Override
    public String modelId() {
        return ttsModel;
    }

    @Override
    public String audioFormat() {
        return ttsFormat;
    }

    @Override
    public String resolveVoice(String stage, String voice) {
        if (TtsProvider.isValidVoice(voice)) {
            return voice;
        }
        if (StageEnum.PRIMARY_LOW.getCode().equals(stage)) {
            return voicePrimaryLow;
        }
        if (StageEnum.PRIMARY_HIGH.getCode().equals(stage)) {
            return voicePrimaryHigh;
        }
        return voiceDefault;
    }

    @Override
    public TtsSynthesizeResult synthesize(TtsSynthesizeRequest request) {
        if (!isConfigured()) {
            return TtsSynthesizeResult.failure(
                    "语音合成服务未配置：请设置环境变量 NEXORA_MIMO_TTS_API_KEY 后重启服务");
        }
        if (request == null || request.text() == null || request.text().isBlank()) {
            return TtsSynthesizeResult.failure("语音合成失败：文本为空");
        }
        try {
            return doSynthesize(request);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return TtsSynthesizeResult.failure("语音合成失败：请求被中断，请稍后重试");
        } catch (Exception e) {
            log.error("MiMo TTS 调用异常", e);
            return TtsSynthesizeResult.failure("语音合成失败：" + e.getMessage());
        }
    }

    private TtsSynthesizeResult doSynthesize(TtsSynthesizeRequest request) throws Exception {
        String voice = TtsProvider.isValidVoice(request.voice()) ? request.voice() : voiceDefault;
        String tone = request.tone() == null ? defaultTone : request.tone();

        JSONObject body = new JSONObject();
        body.put("model", ttsModel);
        JSONArray messages = new JSONArray();
        if (tone != null && !tone.isBlank()) {
            JSONObject user = new JSONObject();
            user.put("role", "user");
            user.put("content", tone);
            messages.add(user);
        }
        JSONObject assistant = new JSONObject();
        assistant.put("role", "assistant");
        assistant.put("content", request.text());
        messages.add(assistant);
        body.put("messages", messages);
        JSONObject audio = new JSONObject();
        audio.put("format", ttsFormat);
        audio.put("voice", voice);
        body.put("audio", audio);
        body.put("stream", false);

        String url = ttsBaseUrl + ttsPath;
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT_SECONDS))
                .build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .header("api-key", ttsApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString()))
                .build();

        HttpResponse<String> response = null;
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            boolean retry;
            try {
                response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                retry = response.statusCode() == 429;
            } catch (Exception e) {
                log.warn("MiMo TTS 请求异常，第 {} 次重试: {}", attempt + 1, e.getMessage());
                retry = true;
            }
            if (retry && attempt < MAX_ATTEMPTS - 1) {
                Thread.sleep(RETRY_BACKOFF_MS);
                continue;
            }
            break;
        }
        if (response == null) {
            String reason = "语音合成失败：3 次请求尝试均网络超时，请检查到 MiMo 语音服务的网络";
            log.warn(reason);
            return TtsSynthesizeResult.failure(reason);
        }
        if (response.statusCode() != 200) {
            String reason = describeFailure(response.statusCode(), response.body());
            String keyHint = "（未检测到有效 NEXORA_MIMO_TTS_API_KEY，当前为占位值：请到 IDEA 运行配置设置并完全重启）";
            if (isConfigured()) {
                keyHint = "（当前 Key 掩码 " + maskHead(ttsApiKey) + "..." + maskTail(ttsApiKey) + "）";
            }
            log.warn("MiMo TTS 接口返回 {}: {}{}", response.statusCode(), truncate(response.body()), keyHint);
            return TtsSynthesizeResult.failure(reason + keyHint);
        }

        JSONObject result = JSON.parseObject(response.body());
        JSONArray choices = result == null ? null : result.getJSONArray("choices");
        if (choices != null && !choices.isEmpty()) {
            JSONObject message = choices.getJSONObject(0).getJSONObject("message");
            JSONObject audioOut = message == null ? null : message.getJSONObject("audio");
            String data = audioOut == null ? null : audioOut.getString("data");
            if (data != null && !data.isBlank()) {
                return TtsSynthesizeResult.success(Base64.getDecoder().decode(normalizeBase64(data)));
            }
        }
        log.warn("MiMo TTS 响应无音频数据: {}", truncate(response.body()));
        return TtsSynthesizeResult.failure("语音合成失败：响应中未找到音频数据");
    }

    /** 去除换行/空白并补齐 padding（上游分块 base64 可能含换行） */
    private String normalizeBase64(String data) {
        String cleaned = data.replaceAll("\\s", "");
        return cleaned + "=".repeat((-cleaned.length()) % 4);
    }

    private String maskHead(String key) {
        return key == null || key.length() < 8 ? "****" : key.substring(0, 4);
    }

    private String maskTail(String key) {
        return key == null || key.length() < 8 ? "****" : key.substring(key.length() - 4);
    }

    private String describeFailure(int statusCode, String body) {
        String detail = truncate(body);
        if (statusCode == 401) {
            return "语音合成失败：MiMo API Key 无效或无权限，请检查 NEXORA_MIMO_TTS_API_KEY";
        }
        if (statusCode == 429) {
            return "语音合成失败：MiMo 请求过快（限流），请稍后重试";
        }
        if (detail.contains("\"error\"")) {
            String message = detail;
            try {
                JSONObject err = JSON.parseObject(detail);
                JSONObject error = err == null ? null : err.getJSONObject("error");
                if (error != null && error.getString("message") != null) {
                    message = error.getString("message");
                }
            } catch (Exception ignored) {
                // 保留原文
            }
            if (message.contains("quota") || message.contains("balance") || message.contains("额度") || message.contains("余额")) {
                return "语音合成失败：MiMo 账户额度不足，请到小米 MiMo 开放平台充值或开通";
            }
            return "语音合成失败：" + message;
        }
        return "语音合成失败：" + detail;
    }

    private String truncate(String text) {
        return text == null ? "" : (text.length() > 300 ? text.substring(0, 300) : text);
    }
}
