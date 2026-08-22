package com.nexora.component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.nexora.entity.enums.StageEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 绘本生成组件：LLM 生成故事分页文案（JSON），逐页调用文生图模型（dashscope qwen-image）生成插图并下载到本地。
 * 产物结构（存 resource_info.ext_json）：{ "type":"PICTURE_BOOK", "pages":[ { "text":"...", "imageFile":"相对路径" } ] }
 * 图像生成失败该页降级为纯文字（imageFile 为空），不阻断整个绘本。
 */
@Slf4j
@Component
public class PictureBookGenerateComponent {

    private static final int MAX_PAGES = 8;
    private static final int IMAGE_TIMEOUT_SECONDS = 120;
    private static final int TASK_POLL_TIMES = 30;
    private static final long TASK_POLL_INTERVAL_MS = 3000;

    private static final String STORY_SYSTEM_PROMPT = """
            你是 K12 人工智能通识课的儿童绘本编辑。学生学段：%s。
            根据用户主题创作一个短小的科普或成长绘本故事，只输出一个 JSON 对象，不要输出任何解释或 Markdown 标记。
            JSON 结构：
            {
              "title": "绘本标题（简短有趣）",
              "pages": [ { "text": "第1页文字（1-2 句，口语化、适合儿童朗读）" }, ... ]
            }
            要求：
            1. pages 为 4-8 页，情节递进，最后一页温暖收尾；
            2. 语言面向小学低年级儿童，画面感强、词汇简单、避免生僻词；
            3. 主题尽量结合人工智能通识内容（如机器人、编程、数据、图像识别等），题目未指明时自选贴近生活的主题；
            4. title 简短，8 字以内。""";

    private final ChatClient chatClient;

    @Value("${project.folder}")
    private String projectFolder;

    @Value("${resource.file-dir}")
    private String resourceFileDir;

    @Value("${project.ai.image.provider:dashscope}")
    private String imageProvider;

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

    public PictureBookGenerateComponent(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 生成绘本故事脚本（分页文案）
     */
    public StoryScript generateStory(String stage, String topic) {
        String stageDesc = stageDesc(stage);
        String systemPrompt = String.format(STORY_SYSTEM_PROMPT, stageDesc);
        String userPrompt = "请围绕主题「" + (topic == null ? "" : topic) + "」创作绘本故事（学段：" + stageDesc + "）。只输出 JSON。";
        String raw;
        try {
            raw = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("绘本故事生成调用失败", e);
            throw new RuntimeException("绘本故事生成失败");
        }
        return parseStory(raw);
    }

    /**
     * 生成一页插图并下载到本地，返回相对文件路径；失败返回 null（调用方降级为纯文字页）
     * 落盘目录：student/{邮箱目录}/picture-book/{月份}/
     */
    public String generatePageImage(String email, String stage, String pageText, String bookTitle, int pageIndex) {
        try {
            String prompt = buildImagePrompt(stage, bookTitle, pageIndex, pageText);
            String imageUrl = callImageApi(prompt);
            if (imageUrl == null || imageUrl.isBlank()) {
                return null;
            }
            return downloadImage(email, imageUrl);
        } catch (Exception e) {
            log.warn("绘本插图生成失败 page={} title={}", pageIndex + 1, bookTitle, e);
            return null;
        }
    }

    private StoryScript parseStory(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new RuntimeException("绘本故事为空");
        }
        String text = raw.trim();
        if (text.startsWith("```")) {
            text = text.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "");
        }
        JSONObject root = JSON.parseObject(text);
        if (root == null) {
            throw new RuntimeException("绘本故事解析失败");
        }
        String title = root.getString("title");
        JSONArray pages = root.getJSONArray("pages");
        if (pages == null || pages.isEmpty()) {
            throw new RuntimeException("绘本缺少页面");
        }
        List<String> texts = new ArrayList<>();
        for (int i = 0; i < pages.size() && i < MAX_PAGES; i++) {
            JSONObject page = pages.getJSONObject(i);
            if (page == null) {
                continue;
            }
            String pageText = page.getString("text");
            if (pageText != null && !pageText.isBlank()) {
                texts.add(pageText.trim());
            }
        }
        if (texts.isEmpty()) {
            throw new RuntimeException("绘本页面文案为空");
        }
        return new StoryScript(title == null ? "" : title, texts);
    }

    private String buildImagePrompt(String stage, String bookTitle, int pageIndex, String pageText) {
        String styleHint = "儿童绘本插画风格，色彩明亮柔和，圆润可爱的角色与场景，简洁构图，无文字";
        String stageDesc = stageDesc(stage);
        return styleHint + "。绘本《" + bookTitle + "》第 " + (pageIndex + 1)
                + " 页画面（面向" + stageDesc + "儿童）：" + pageText;
    }

    /**
     * 调用文生图模型（dashscope qwen-image 同步 + 异步任务轮询）
     */
    private String callImageApi(String prompt) throws Exception {
        if ("dashscope".equalsIgnoreCase(imageProvider)) {
            return callDashscope(prompt);
        }
        throw new RuntimeException("不支持的图像供应商: " + imageProvider);
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
        parameters.put("size", "1024*768");
        parameters.put("n", 1);
        parameters.put("prompt_extend", promptExtend);
        JSONObject input = new JSONObject();
        input.put("messages", messages);
        input.put("parameters", parameters);
        JSONObject body = new JSONObject();
        body.put("model", imageModel);
        body.put("input", input);

        String url = imageBaseUrl + imagePath;
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(IMAGE_TIMEOUT_SECONDS))
                .header("Authorization", "Bearer " + imageApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString()))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            log.warn("文生图接口返回 {}: {}", response.statusCode(), truncate(response.body()));
            return null;
        }
        JSONObject result = JSON.parseObject(response.body());
        if (result == null) {
            return null;
        }
        JSONObject output = result.getJSONObject("output");
        if (output == null) {
            return null;
        }
        // 同步模式：直接返回结果 URL
        JSONArray results = output.getJSONArray("results");
        if (results != null && !results.isEmpty()) {
            JSONObject first = results.getJSONObject(0);
            if (first != null) {
                String urlValue = first.getString("url");
                if (urlValue != null && !urlValue.isBlank()) {
                    return urlValue;
                }
            }
        }
        // 异步任务模式：轮询任务结果
        String taskId = output.getString("task_id");
        if (taskId == null || taskId.isBlank()) {
            return null;
        }
        return pollTask(client, taskId);
    }

    private String pollTask(HttpClient client, String taskId) throws Exception {
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
                return null;
            }
            JSONObject body = JSON.parseObject(response.body());
            JSONObject output = body == null ? null : body.getJSONObject("output");
            if (output == null) {
                return null;
            }
            String status = output.getString("task_status");
            if ("SUCCEEDED".equalsIgnoreCase(status)) {
                JSONArray results = output.getJSONArray("results");
                if (results != null && !results.isEmpty()) {
                    JSONObject first = results.getJSONObject(0);
                    if (first != null) {
                        return first.getString("url");
                    }
                }
                return null;
            }
            if ("FAILED".equalsIgnoreCase(status) || "CANCELED".equalsIgnoreCase(status)) {
                log.warn("文生图任务失败 taskId={} status={}", taskId, status);
                return null;
            }
        }
        log.warn("文生图任务轮询超时 taskId={}", taskId);
        return null;
    }

    private String downloadImage(String email, String imageUrl) throws Exception {
        String monthDir = java.time.LocalDate.now().toString().replace("-", "");
        Path targetDir = Paths.get(projectFolder, resourceFileDir, "student", emailDir(email), "picture-book", monthDir);
        Files.createDirectories(targetDir);
        String fileName = UUID.randomUUID().toString().replace("-", "") + ".png";
        Path target = targetDir.resolve(fileName);
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(imageUrl))
                .timeout(Duration.ofSeconds(120))
                .GET()
                .build();
        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() != 200) {
            log.warn("绘本插图下载失败 url={} status={}", imageUrl, response.statusCode());
            return null;
        }
        try (InputStream input = response.body()) {
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return resourceFileDir + "/student/" + emailDir(email) + "/picture-book/" + monthDir + "/" + fileName;
    }

    /**
     * 邮箱目录名：小写，@ 转 _at_
     */
    private String emailDir(String email) {
        if (email == null || email.isBlank()) {
            return "unknown";
        }
        return email.trim().toLowerCase(java.util.Locale.ROOT).replace("@", "_at_");
    }

    private String stageDesc(String stage) {
        if (stage == null) {
            return "未知学段";
        }
        for (StageEnum item : StageEnum.values()) {
            if (item.getCode().equals(stage)) {
                return item.getDesc();
            }
        }
        return "未知学段";
    }

    private String truncate(String text) {
        return text == null ? "" : (text.length() > 300 ? text.substring(0, 300) : text);
    }

    public record StoryScript(String title, List<String> pages) {
    }
}