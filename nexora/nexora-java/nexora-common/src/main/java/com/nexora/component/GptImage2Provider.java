package com.nexora.component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * gpt-image-2 文生图实现：OpenAI 兼容网关（img.zikl.dev）/v1/images/generations → data[0].url。
 * 由 {@link ImageProviderRouter} 按 system_config 切换使用，不直接参与启动装配选择。
 */
@Slf4j
@Component
public class GptImage2Provider implements ImageProvider {

    private static final int TIMEOUT_SECONDS = 240;
    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_BACKOFF_MS = 1500;

    @Value("${project.ai.image.gpt-model:gpt-image-2}")
    private String gptModel;

    @Value("${project.ai.image.gpt-base-url:https://img.zikl.dev}")
    private String gptBaseUrl;

    @Value("${project.ai.image.gpt-path:/v1/images/generations}")
    private String gptPath;

    @Value("${project.ai.image.gpt-api-key:}")
    private String gptApiKey;

    @Value("${project.ai.image.gpt-size:1024x1024}")
    private String gptSize;

    @Override
    public int maxConcurrency() {
        return 1;
    }

    @Override
    public ImageGenerateResult generate(String prompt) {
        try {
            return doGenerate(prompt);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ImageGenerateResult.failure("图片生成失败：gpt-image-2 请求被中断，请稍后重试");
        } catch (Exception e) {
            log.error("gpt-image-2 文生图调用异常", e);
            return ImageGenerateResult.failure("图片生成失败：" + e.getMessage());
        }
    }

    private ImageGenerateResult doGenerate(String prompt) throws Exception {
        JSONObject body = new JSONObject();
        body.put("model", gptModel);
        body.put("prompt", prompt);
        body.put("size", gptSize);
        body.put("n", 1);
        body.put("response_format", "url");
        String url = gptBaseUrl + gptPath;
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .header("Authorization", "Bearer " + gptApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString()))
                .build();
        HttpResponse<String> response = null;
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            boolean retry;
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
                retry = response.statusCode() == 429;
            } catch (Exception e) {
                log.warn("gpt-image-2 文生图请求异常，第 {} 次重试: {}", attempt + 1, e.getMessage());
                retry = true;
            }
            if (retry && attempt < MAX_ATTEMPTS - 1) {
                Thread.sleep(RETRY_BACKOFF_MS);
                continue;
            }
            break;
        }
        if (response == null) {
            String reason = "图片生成失败：3 次请求尝试均网络超时，请检查本机到 gpt-image-2 网关的网络";
            log.warn(reason);
            return ImageGenerateResult.failure(reason);
        }
        if (response.statusCode() != 200) {
            String reason = describeFailure(response.statusCode(), response.body());
            String keyHint = "****".equals(maskHead(gptApiKey))
                    ? "（未检测到有效 NEXORA_GPT_IMAGE_API_KEY，当前为占位值：请到 IDEA 运行配置设置并完全重启）"
                    : "（当前 Key 掩码 " + maskHead(gptApiKey) + "..." + maskTail(gptApiKey) + "）";
            log.warn("gpt-image-2 文生图接口返回 {}: {}{}",
                    response.statusCode(), truncate(response.body()), keyHint);
            return ImageGenerateResult.failure(reason + keyHint);
        }
        JSONObject result = JSON.parseObject(response.body());
        if (result == null) {
            log.warn("gpt-image-2 文生图响应无法解析: {}", truncate(response.body()));
            return ImageGenerateResult.failure("图片生成失败：gpt-image-2 响应无法解析");
        }
        JSONArray data = result.getJSONArray("data");
        if (data != null && !data.isEmpty()) {
            String image = data.getJSONObject(0) == null ? null : data.getJSONObject(0).getString("url");
            if (image != null && !image.isBlank()) {
                return ImageGenerateResult.success(image);
            }
        }
        log.warn("gpt-image-2 文生图响应无图片: {}", truncate(response.body()));
        return ImageGenerateResult.failure("图片生成失败：gpt-image-2 响应中未找到图片");
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
            return "图片生成失败：gpt-image-2 API Key 无效或无权限，请检查 NEXORA_GPT_IMAGE_API_KEY";
        }
        if (statusCode == 429) {
            return "图片生成失败：gpt-image-2 请求过快（限流），请稍后重试";
        }
        if (statusCode == 402) {
            return "图片生成失败：gpt-image-2 账户额度不足，请到 img.zikl.dev 充值或开通";
        }
        // OpenAI 兼容错误体: {"error":{"message":"..."}}
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
                return "图片生成失败：gpt-image-2 账户额度不足，请到 img.zikl.dev 充值或开通";
            }
            return "图片生成失败：" + message;
        }
        return "图片生成失败：" + detail;
    }

    private String truncate(String text) {
        return text == null ? "" : (text.length() > 300 ? text.substring(0, 300) : text);
    }
}