package com.nexora.component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * 百炼（DashScope）文生图实现：qwen-image 同步接口 + 异步任务轮询兜底。
 * 通过配置 project.ai.image.provider=dashscope 自动装配；缺省时也使用本实现。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "project.ai.image.provider", havingValue = "dashscope", matchIfMissing = true)
public class DashscopeImageProvider implements ImageProvider {

    /** qwen-image-2.0-pro 实测约 112s 出图，放宽到 240s，避免把正常生图误判为超时 */
    private static final int IMAGE_TIMEOUT_SECONDS = 240;
    /** 异步任务轮询：3 秒一次，最长约 3 分钟 */
    private static final int TASK_POLL_TIMES = 60;
    private static final long TASK_POLL_INTERVAL_MS = 3000;
    private static final int MAX_ATTEMPTS = 3;
    private static final long BACKOFF_429_MS = 10_000;
    private static final long BACKOFF_NETWORK_MS = 5_000;
    private static final long RATE_LIMIT_WAIT_MS = 90_000;

    private final DashscopeRateLimiter dashscopeRateLimiter;

    @Value("${project.ai.image.model:qwen-image-3.0}")
    private String imageModel;

    @Value("${project.ai.image.base-url:https://dashscope.aliyuncs.com}")
    private String imageBaseUrl;

    @Value("${project.ai.image.path:/api/v1/services/aigc/multimodal-generation/generation}")
    private String imagePath;

    @Value("${project.ai.image.api-key:}")
    private String imageApiKey;

    @Value("${project.ai.image.prompt-extend:true}")
    private boolean promptExtend;

    public DashscopeImageProvider(DashscopeRateLimiter dashscopeRateLimiter) {
        this.dashscopeRateLimiter = dashscopeRateLimiter;
    }

    @Override
    public ImageGenerateResult generate(String prompt) {
        try {
            return doGenerate(prompt);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ImageGenerateResult.failure("图片生成失败：百炼请求被中断，请稍后重试");
        } catch (Exception e) {
            log.error("百炼文生图调用异常", e);
            return ImageGenerateResult.failure("图片生成失败：" + e.getMessage());
        }
    }

    private ImageGenerateResult doGenerate(String prompt) throws Exception {
        JSONObject contentItem = new JSONObject();
        contentItem.put("text", prompt);
        JSONArray content = new JSONArray();
        content.add(contentItem);
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", content);
        JSONArray messages = new JSONArray();
        messages.add(message);
        JSONObject parameters = new JSONObject();
        parameters.put("size", "1024*768");
        parameters.put("n", 1);
        parameters.put("prompt_extend", promptExtend);
        JSONObject input = new JSONObject();
        input.put("messages", messages);
        input.put("parameters", parameters);
        JSONObject body = new JSONObject();
        body.put("model", imageModel);
        body.put("input", input);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(imageBaseUrl + imagePath))
                .timeout(Duration.ofSeconds(IMAGE_TIMEOUT_SECONDS))
                .header("Authorization", "Bearer " + imageApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString()))
                .build();
        // 每次真实 HTTP 发送前先领取 Redis 分布式令牌（web/admin 共用同一账号额度）；
        // 429 与网络超时采用不同退避，总尝试次数仍为 3 次
        HttpResponse<String> response = null;
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            if (!dashscopeRateLimiter.acquireDashscope(imageModel, RATE_LIMIT_WAIT_MS)) {
                String reason = "图片生成失败：百炼图片额度繁忙，请稍后重试";
                log.warn("{} model={}", reason, imageModel);
                return ImageGenerateResult.failure(reason);
            }
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (Exception e) {
                log.warn("文生图请求异常，第 {} 次尝试: {}", attempt + 1, e.getMessage());
                response = null;
                if (attempt < MAX_ATTEMPTS - 1) {
                    Thread.sleep(BACKOFF_NETWORK_MS);
                    continue;
                }
                break;
            }
            if (response.statusCode() == 429) {
                if (attempt < MAX_ATTEMPTS - 1) {
                    log.warn("百炼文生图触发限流，第 {} 次尝试后 {}s 重试",
                            attempt + 1, BACKOFF_429_MS / 1000);
                    Thread.sleep(BACKOFF_429_MS);
                    continue;
                }
                break;
            }
            if (response.statusCode() != 200) {
                break;
            }
            break;
        }
        if (response == null) {
            String reason = "图片生成失败：3 次请求尝试均网络超时，请检查本机到百炼的网络";
            log.warn(reason);
            return ImageGenerateResult.failure(reason);
        }
        if (response.statusCode() != 200) {
            String reason = describeFailure(response.statusCode(), response.body());
            log.warn("文生图接口返回 {}: {}", response.statusCode(), truncate(response.body()));
            return ImageGenerateResult.failure(reason);
        }
        JSONObject result = JSON.parseObject(response.body());
        if (result == null) {
            log.warn("文生图响应无法解析: {}", truncate(response.body()));
            return ImageGenerateResult.failure("图片生成失败：文生图响应无法解析");
        }
        JSONObject output = result.getJSONObject("output");
        if (output == null) {
            log.warn("文生图响应缺少 output: {}", truncate(response.body()));
            return ImageGenerateResult.failure("图片生成失败：文生图响应缺少 output");
        }
        // 同步模式官方结构：output.choices[0].message.content[0].image
        String imageUrl = extractImageFromChoices(output);
        if (imageUrl != null) {
            return ImageGenerateResult.success(imageUrl);
        }
        // 兼容旧版 results 结构（部分网关）
        JSONArray results = output.getJSONArray("results");
        if (results != null && !results.isEmpty()) {
            JSONObject first = results.getJSONObject(0);
            if (first != null) {
                String urlValue = first.getString("url");
                if (urlValue != null && !urlValue.isBlank()) {
                    return ImageGenerateResult.success(urlValue);
                }
            }
        }
        // 异步任务模式：轮询任务结果
        String taskId = output.getString("task_id");
        if (taskId == null || taskId.isBlank()) {
            log.warn("文生图响应无结果无任务ID: {}", truncate(response.body()));
            return ImageGenerateResult.failure("图片生成失败：文生图响应中未找到图片");
        }
        return pollTask(client, taskId);
    }

    private ImageGenerateResult pollTask(HttpClient client, String taskId) throws Exception {
        String taskUrl = imageBaseUrl + "/api/v1/tasks/" + taskId;
        for (int i = 0; i < TASK_POLL_TIMES; i++) {
            Thread.sleep(TASK_POLL_INTERVAL_MS);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(taskUrl))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + imageApiKey)
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return ImageGenerateResult.failure("图片生成失败：百炼任务查询失败（HTTP " + response.statusCode() + "）");
            }
            JSONObject body = JSON.parseObject(response.body());
            JSONObject output = body == null ? null : body.getJSONObject("output");
            if (output == null) {
                return ImageGenerateResult.failure("图片生成失败：百炼任务查询响应缺少 output");
            }
            String status = output.getString("task_status");
            if ("SUCCEEDED".equalsIgnoreCase(status)) {
                String imageUrl = extractImageFromChoices(output);
                if (imageUrl != null) {
                    return ImageGenerateResult.success(imageUrl);
                }
                JSONArray results = output.getJSONArray("results");
                if (results != null && !results.isEmpty()) {
                    JSONObject first = results.getJSONObject(0);
                    if (first != null) {
                        return ImageGenerateResult.success(first.getString("url"));
                    }
                }
                log.warn("文生图任务成功但无结果图 taskId={}", taskId);
                return ImageGenerateResult.failure("图片生成失败：百炼任务成功但未返回图片");
            }
            if ("FAILED".equalsIgnoreCase(status) || "CANCELED".equalsIgnoreCase(status)) {
                log.warn("文生图任务失败 taskId={} status={}", taskId, status);
                return ImageGenerateResult.failure("图片生成失败：图像生成任务未完成（" + status + "），请稍后重试");
            }
        }
        log.warn("文生图任务轮询超时 taskId={}", taskId);
        return ImageGenerateResult.failure("图片生成失败：等待百炼图像任务超时，请稍后重试");
    }

    /**
     * 把接口错误转为用户可读的失败原因（额度/限流/鉴权等）
     */
    private String describeFailure(int statusCode, String body) {
        String detail = truncate(body);
        if (detail.contains("FreeTierOnly") || detail.contains("free quota")) {
            return "图片生成失败：百炼免费额度已用完，请到阿里云百炼控制台充值或关闭「仅使用免费额度」后重试";
        }
        if (detail.contains("RateQuota") || statusCode == 429) {
            return "图片生成失败：请求过于频繁（限流），请稍后重试";
        }
        if (detail.contains("InvalidApiKey") || detail.contains("PermissionDenied")) {
            return "图片生成失败：API Key 无效或无权限，请检查 NEXORA_DASHSCOPE_API_KEY";
        }
        return "图片生成失败：" + detail;
    }

    /**
     * 从 output.choices[0].message.content[0].image 提取结果图 URL（官方同步/异步任务结果统一结构）
     */
    private String extractImageFromChoices(JSONObject output) {
        if (output == null) {
            return null;
        }
        JSONArray choices = output.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            return null;
        }
        JSONObject message = choices.getJSONObject(0) == null ? null : choices.getJSONObject(0).getJSONObject("message");
        if (message == null) {
            return null;
        }
        JSONArray content = message.getJSONArray("content");
        if (content == null || content.isEmpty()) {
            return null;
        }
        return content.getJSONObject(0) == null ? null : content.getJSONObject(0).getString("image");
    }

    private String truncate(String text) {
        return text == null ? "" : (text.length() > 300 ? text.substring(0, 300) : text);
    }
}
