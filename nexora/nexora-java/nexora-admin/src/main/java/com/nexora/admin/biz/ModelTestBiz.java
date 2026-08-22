package com.nexora.admin.biz;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.nexora.exception.BusinessException;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * 模型连通性验证（开发/排障用）：DeepSeek 对话 / 百炼向量 / 百炼文生图
 */
@Slf4j
@Service
public class ModelTestBiz {

    private static final int IMAGE_TIMEOUT_SECONDS = 30;
    private static final int IMAGE_RETRY_TIMES = 2;

    @Resource
    private ChatClient chatClient;

    @Resource
    private EmbeddingModel embeddingModel;

    @Value("${spring.ai.openai.chat.options.model:deepseek-v4-flash}")
    private String chatModel;

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

    /** 豆包(火山方舟 ARK) 配置 */
    @Value("${project.ai.image.provider:dashscope}")
    private String imageProvider;

    @Value("${project.ai.image.ark-model:doubao-seedream-5-0-pro-260628}")
    private String arkModel;

    @Value("${project.ai.image.ark-base-url:https://ark.cn-beijing.volces.com/api/v3}")
    private String arkBaseUrl;

    @Value("${project.ai.image.ark-api-key:}")
    private String arkApiKey;

    /**
     * 1) 对话模型连通性
     */
    public String testChat(String text) {
        if (StringTools.isEmpty(text)) {
            throw new BusinessException("请输入测试文本");
        }
        try {
            String content = chatClient.prompt()
                    .system("你是连通性测试助手，请简短回复“模型调用成功”，并复述用户的话。")
                    .user(text)
                    .call()
                    .content();
            if (content == null || content.isBlank()) {
                throw new BusinessException("模型返回空内容（模型可连通，但未返回文本）");
            }
            return content.trim();
        } catch (BusinessException e) {
            log.error("模型验证-对话失败: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("模型验证-对话异常", e);
            throw new BusinessException("对话模型调用失败：" + e.getMessage());
        }
    }

    /**
     * 2) 向量模型连通性
     */
    public EmbeddingTestVO testEmbedding(String text) {
        if (StringTools.isEmpty(text)) {
            throw new BusinessException("请输入测试文本");
        }
        try {
            EmbeddingResponse response = embeddingModel.call(new EmbeddingRequest(
                    List.of(text), EmbeddingOptions.builder().build()));
            Embedding embedding = response.getResult();
            if (embedding == null) {
                throw new BusinessException("向量模型未返回结果");
            }
            float[] values = embedding.getOutput();
            EmbeddingTestVO vo = new EmbeddingTestVO();
            vo.setDimension(values == null ? 0 : values.length);
            vo.setSample(values == null || values.length == 0 ? "" : formatSample(values));
            return vo;
        } catch (BusinessException e) {
            log.error("模型验证-向量失败: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("模型验证-向量异常", e);
            throw new BusinessException("向量模型调用失败：" + e.getMessage());
        }
    }

    /**
     * 3) 文生图模型连通性（dashscope qwen-image 同步接口；成功返回临时 URL，24 小时有效）
     */
    public ImageTestVO testImage(String prompt) {
        if (StringTools.isEmpty(prompt)) {
            throw new BusinessException("请输入画面描述");
        }
        try {
            String resultUrl = "dashscope".equalsIgnoreCase(imageProvider)
                    ? callDashscope(prompt.trim())
                    : callArk(prompt.trim());
            ImageTestVO vo = new ImageTestVO();
            vo.setSuccess(resultUrl != null);
            vo.setUrl(resultUrl);
            vo.setMessage(resultUrl == null ? "文生图调用失败（详见后端日志）" : "图片生成成功（临时链接 24 小时内有效）");
            if (resultUrl == null) {
                log.warn("模型验证-文生图失败: 未取到生成图片");
            } else {
                log.info("模型验证-文生图成功");
            }
            return vo;
        } catch (BusinessException e) {
            log.error("模型验证-文生图失败: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("模型验证-文生图异常", e);
            throw new BusinessException("文生图调用失败：" + e.getMessage());
        }
    }

    private String callDashscope(String prompt) throws Exception {
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
        parameters.put("size", "1024*1024");
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
        HttpResponse<String> response = null;
        for (int attempt = 0; attempt <= IMAGE_RETRY_TIMES; attempt++) {
            boolean retry;
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
                retry = response.statusCode() == 429;
            } catch (Exception e) {
                log.warn("模型验证-文生图请求异常，第 {} 次重试: {}", attempt + 1, e.getMessage());
                retry = true;
            }
            if (retry && attempt < IMAGE_RETRY_TIMES) {
                Thread.sleep(1500);
                continue;
            }
            break;
        }
        if (response == null) {
            throw new BusinessException("文生图请求 3 次尝试均超时/网络异常，请检查本机到百炼的网络或代理设置");
        }
        if (response.statusCode() != 200) {
            String detail = response.body() == null ? "" : response.body();
            if (detail.contains("FreeTierOnly") || detail.contains("free quota")) {
                throw new BusinessException("免费额度已用完，请到百炼控制台充值或关闭「仅使用免费额度」");
            }
            if (detail.contains("InvalidApiKey") || detail.contains("PermissionDenied")) {
                throw new BusinessException("API Key 无效或无权限，请检查 NEXORA_DASHSCOPE_API_KEY / NEXORA_IMAGE_API_KEY");
            }
            throw new BusinessException("文生图接口返回 " + response.statusCode() + ": " + truncate(detail));
        }
        JSONObject result = JSON.parseObject(response.body());
        if (result == null) {
            throw new BusinessException("文生图响应无法解析");
        }
        JSONObject output = result.getJSONObject("output");
        if (output == null) {
            throw new BusinessException("文生图响应缺少 output");
        }
        JSONArray choices = output.getJSONArray("choices");
        if (choices != null && !choices.isEmpty()) {
            JSONObject choiceMessage = choices.getJSONObject(0) == null ? null : choices.getJSONObject(0).getJSONObject("message");
            JSONArray choiceContent = choiceMessage == null ? null : choiceMessage.getJSONArray("content");
            if (choiceContent != null && !choiceContent.isEmpty()) {
                String image = choiceContent.getJSONObject(0) == null ? null : choiceContent.getJSONObject(0).getString("image");
                if (image != null && !image.isBlank()) {
                    return image;
                }
            }
        }
        throw new BusinessException("文生图响应中未找到生成图片");
    }

    /**
     * 豆包(火山方舟 ARK) 文生图：OpenAI 兼容 /images/generations → data[0].url
     */
    private String callArk(String prompt) throws Exception {
        JSONObject body = new JSONObject();
        body.put("model", arkModel);
        body.put("prompt", prompt);
        body.put("response_format", "url");
        body.put("output_format", "png");
        body.put("size", "1.5K");
        body.put("n", 1);
        body.put("watermark", false);
        String url = arkBaseUrl + "/images/generations";
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(90))
                .header("Authorization", "Bearer " + arkApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString()))
                .build();
        HttpResponse<String> response = null;
        for (int attempt = 0; attempt <= IMAGE_RETRY_TIMES; attempt++) {
            boolean retry;
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
                retry = response.statusCode() == 429;
            } catch (Exception e) {
                log.warn("模型验证-豆包文生图请求异常，第 {} 次重试: {}", attempt + 1, e.getMessage());
                retry = true;
            }
            if (retry && attempt < IMAGE_RETRY_TIMES) {
                Thread.sleep(1500);
                continue;
            }
            break;
        }
        if (response == null) {
            throw new BusinessException("豆包文生图 3 次尝试均超时/网络异常，请检查本机到方舟的网络或代理设置");
        }
        if (response.statusCode() != 200) {
            String masked = (arkApiKey == null || arkApiKey.length() < 8)
                    ? "****"
                    : arkApiKey.substring(0, 4) + "..." + arkApiKey.substring(arkApiKey.length() - 4);
            String hint = "****".equals(masked)
                    ? "（未检测到有效的 NEXORA_ARK_API_KEY，当前为占位值：请到 IDEA 运行配置设置该变量并完全重启）"
                    : "（当前 Key 掩码 " + masked + "，请与控制台复制的 Key 核对）";
            throw new BusinessException("豆包文生图接口返回 " + response.statusCode() + ": "
                    + truncate(response.body()) + hint);
        }
        JSONObject result = JSON.parseObject(response.body());
        JSONArray data = result == null ? null : result.getJSONArray("data");
        if (data != null && !data.isEmpty()) {
            String image = data.getJSONObject(0) == null ? null : data.getJSONObject(0).getString("url");
            if (image != null && !image.isBlank()) {
                return image;
            }
        }
        throw new BusinessException("豆包文生图响应中未找到图片");
    }

    private String formatSample(float[] values) {
        StringBuilder sb = new StringBuilder("[");
        int count = Math.min(5, values.length);
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(String.format("%.4f", values[i]));
        }
        sb.append(count < values.length ? ", ..." : "");
        sb.append("]");
        return sb.toString();
    }

    private String truncate(String text) {
        return text == null ? "" : (text.length() > 400 ? text.substring(0, 400) : text);
    }

    public static class EmbeddingTestVO {
        private int dimension;
        private String sample;

        public int getDimension() {
            return dimension;
        }

        public void setDimension(int dimension) {
            this.dimension = dimension;
        }

        public String getSample() {
            return sample;
        }

        public void setSample(String sample) {
            this.sample = sample;
        }
    }

    public static class ImageTestVO {
        private boolean success;
        private String url;
        private String message;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}