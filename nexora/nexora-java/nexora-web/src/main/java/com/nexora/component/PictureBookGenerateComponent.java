package com.nexora.component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.nexora.entity.enums.StageEnum;
import com.nexora.utils.StringTools;
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
    /** 文生图单次请求超时(dashscope 同步)：快速失败，避免逐页串行时整本绘本长时间卡住（6 页最坏约 3.5 分钟封顶） */
    private static final int IMAGE_TIMEOUT_SECONDS = 30;
    /** 豆包文生图单次请求超时：生成+排队通常 20-60s，放宽到 90s */
    private static final int ARK_TIMEOUT_SECONDS = 90;
    /** 失败图片下载超时（生成后的结果图较大，单独放宽） */
    private static final int IMAGE_DOWNLOAD_TIMEOUT_SECONDS = 60;
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
            4. title 简短，8 字以内；
            5. 所有角色必须为原创角色，绝不使用任何受版权保护的知名角色或 IP（如白雪公主、灰姑娘、睡美人、艾莎、米老鼠等）及其标志性情节（如毒苹果、水晶鞋、王后魔镜等）；若用户主题涉及此类角色，请自动改写为原创中性角色与情节，页面文字中不出现任何知名动画人物的名称。""";

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

    /** 豆包(火山方舟 ARK, OpenAI 兼容) 配置 */
    @Value("${project.ai.image.ark-model:doubao-seedream-5-0-pro-260628}")
    private String arkModel;

    @Value("${project.ai.image.ark-base-url:https://ark.cn-beijing.volces.com/api/v3}")
    private String arkBaseUrl;

    @Value("${project.ai.image.ark-api-key:}")
    private String arkApiKey;

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

    /** 最近一次插图失败原因（单次绘本生成内串行使用） */
    private final java.util.concurrent.atomic.AtomicReference<String> lastFailure = new java.util.concurrent.atomic.AtomicReference<>();

    /**
     * 主题净化：把含知名 IP/版权角色的主题解构为原创中性表述（如"米老鼠"→"一只爱冒险的可爱小老鼠"）。
     * 净化失败时回落原主题（故事/生图提示词里仍有原创化约束兜底）。
     */
    public String sanitizeTopic(String topic) {
        if (StringTools.isEmpty(topic)) {
            return topic;
        }
        try {
            String raw = chatClient.prompt()
                    .system("你是儿童绘本内容安全编辑。把用户给出的绘本主题改写为完全原创、不侵权的表述。规则："
                            + "1) 若主题含任何知名动画/童话角色或 IP（如米老鼠、唐老鸭、白雪公主、灰姑娘、艾莎、汪汪队、小猪佩奇、奥特曼、宝可梦等），"
                            + "将其形象拆解为通用元素（例如：米老鼠→一只戴小红帽、穿黄鞋子、爱冒险的可爱小老鼠；唐老鸭→一只穿水手服的滑稽鸭子；"
                            + "白雪公主→一位善良勇敢的森林女孩）并用原创中性名称重写；"
                            + "2) 保留主题的乐趣类型（冒险/友谊/魔幻/科普等），不得输出任何知名角色名或 IP 名；"
                            + "3) 只能输出一句话主题（30 字以内），不要解释、不要引号、不要换行。")
                    .user(topic)
                    .call()
                    .content();
            String cleaned = raw == null ? null : raw.trim().replaceAll("[\"“”'']", "");
            if (cleaned == null || cleaned.isBlank() || cleaned.length() > 60) {
                return topic;
            }
            return cleaned;
        } catch (Exception e) {
            log.warn("绘本主题净化失败，使用原主题: {}", topic, e);
            return topic;
        }
    }

    /**
     * 生成一页插图并下载到本地，返回相对文件路径；失败返回 null（调用方降级为纯文字页）
     * 落盘目录：student/{邮箱目录}/picture-book/{月份}/
     */
    public String generatePageImage(String email, String stage, String pageText, String bookTitle, int pageIndex) {
        lastFailure.set(null);
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

    /**
     * 最近一次插图失败原因（供外部记录到产物中，用户可见）
     */
    public String getLastFailureReason() {
        return lastFailure.get();
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
        String styleHint = "儿童绘本插画风格，色彩明亮柔和，圆润可爱的原创角色与场景，简洁构图，无文字。"
                + "严禁出现任何知名童话或动画 IP 角色及其标志性元素（如白雪公主、灰姑娘、爱莎、米老鼠、魔镜、毒苹果等），全部使用原创角色与场景设计。";
        String stageDesc = stageDesc(stage);
        return styleHint + "。绘本《" + bookTitle + "》第 " + (pageIndex + 1)
                + " 页画面（面向" + stageDesc + "儿童）：" + pageText;
    }

    /**
     * 调用文生图模型：dashscope(qwen-image) / ark(豆包 seedream, OpenAI 兼容)
     */
    private String callImageApi(String prompt) throws Exception {
        if ("dashscope".equalsIgnoreCase(imageProvider)) {
            return callDashscope(prompt);
        }
        if ("ark".equalsIgnoreCase(imageProvider)) {
            return callArk(prompt);
        }
        throw new RuntimeException("不支持的图像供应商: " + imageProvider);
    }

    /**
     * 豆包(火山方舟 ARK) 文生图：OpenAI 兼容 /images/generations，
     * 响应 {data:[{url}]}；429/网络异常自动重试；失败原因写入 lastFailure
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
        JSONObject optimize = new JSONObject();
        optimize.put("mode", "fast");
        body.put("optimize_prompt_options", optimize);
        String url = arkBaseUrl + "/images/generations";
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(ARK_TIMEOUT_SECONDS))
                .header("Authorization", "Bearer " + arkApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString()))
                .build();
        HttpResponse<String> response = null;
        for (int attempt = 0; attempt < 3; attempt++) {
            boolean retry;
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
                retry = response.statusCode() == 429;
            } catch (Exception e) {
                log.warn("豆包文生图请求异常，第 {} 次重试: {}", attempt + 1, e.getMessage());
                retry = true;
            }
            if (retry && attempt < 2) {
                Thread.sleep(1500);
                continue;
            }
            break;
        }
        if (response == null) {
            String reason = "图片生成失败：3 次请求尝试均网络超时，请检查本机到豆包(方舟)的网络";
            log.warn(reason);
            lastFailure.set(reason);
            return null;
        }
        if (response.statusCode() != 200) {
            String reason = describeArkFailure(response.statusCode(), response.body());
            String maskedHead = maskHead(arkApiKey);
            String maskedTail = maskTail(arkApiKey);
            String keyHint = "****".equals(maskedHead)
                    ? "（未检测到有效 NEXORA_ARK_API_KEY，当前为占位值：请到 IDEA 运行配置设置并完全重启）"
                    : "（当前 Key 掩码 " + maskedHead + "..." + maskedTail + "）";
            log.warn("豆包文生图接口返回 {}: {}{}",
                    response.statusCode(), truncate(response.body()), keyHint);
            lastFailure.set(reason + keyHint);
            return null;
        }
        JSONObject result = JSON.parseObject(response.body());
        if (result == null) {
            log.warn("豆包文生图响应无法解析: {}", truncate(response.body()));
            lastFailure.set("图片生成失败：豆包响应无法解析");
            return null;
        }
        JSONArray data = result.getJSONArray("data");
        if (data != null && !data.isEmpty()) {
            String image = data.getJSONObject(0) == null ? null : data.getJSONObject(0).getString("url");
            if (image != null && !image.isBlank()) {
                return image;
            }
        }
        log.warn("豆包文生图响应无图片: {}", truncate(response.body()));
        lastFailure.set("图片生成失败：豆包响应中未找到图片");
        return null;
    }

    private String maskHead(String key) {
        return key == null || key.length() < 8 ? "****" : key.substring(0, 4);
    }

    private String maskTail(String key) {
        return key == null || key.length() < 8 ? "****" : key.substring(key.length() - 4);
    }

    private String describeArkFailure(int statusCode, String body) {
        String detail = truncate(body);
        if (statusCode == 401) {
            return "图片生成失败：豆包 API Key 无效或无权限，请检查 NEXORA_ARK_API_KEY";
        }
        if (statusCode == 429) {
            return "图片生成失败：豆包请求过快（限流），请稍后重试";
        }
        if (detail.contains("SensitiveContent") || detail.contains("PolicyViolation") || detail.contains("copyright")) {
            return "图片生成被平台内容安全拦截（可能涉及版权角色或不当内容），建议更换主题或改写页面描述";
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
                return "图片生成失败：豆包账户额度不足，请到火山方舟控制台充值或开通";
            }
            return "图片生成失败：" + message;
        }
        return "图片生成失败：" + detail;
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
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(IMAGE_TIMEOUT_SECONDS))
                .header("Authorization", "Bearer " + imageApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString()))
                .build();
        // 429 限流与网络抖动(超时/连接异常)均自动重试：总 3 次尝试，间隔 1.5s
        HttpResponse<String> response = null;
        for (int attempt = 0; attempt < 3; attempt++) {
            boolean retry;
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
                retry = response.statusCode() == 429;
            } catch (Exception e) {
                log.warn("文生图请求异常，第 {} 次重试: {}", attempt + 1, e.getMessage());
                retry = true;
            }
            if (retry && attempt < 2) {
                Thread.sleep(1500);
                continue;
            }
            break;
        }
        if (response == null) {
            String reason = "图片生成失败：3 次请求尝试均网络超时，请检查本机到百炼的网络";
            log.warn(reason);
            lastFailure.set(reason);
            return null;
        }
        if (response.statusCode() != 200) {
            String reason = "文生图接口返回 " + response.statusCode() + ": " + truncate(response.body());
            log.warn(reason);
            lastFailure.set(describeFailure(response.statusCode(), response.body()));
            return null;
        }
        JSONObject result = JSON.parseObject(response.body());
        if (result == null) {
            log.warn("文生图响应无法解析: {}", truncate(response.body()));
            return null;
        }
        JSONObject output = result.getJSONObject("output");
        if (output == null) {
            log.warn("文生图响应缺少 output: {}", truncate(response.body()));
            return null;
        }
        // 同步模式官方结构：output.choices[0].message.content[0].image
        String imageUrl = extractImageFromChoices(output);
        if (imageUrl != null) {
            return imageUrl;
        }
        // 兼容旧版 results 结构（部分网关）
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
            log.warn("文生图响应无结果无任务ID: {}", truncate(response.body()));
            return null;
        }
        return pollTask(client, taskId);
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
                // 任务成功结果同样为 choices 结构；兼容旧 results 结构
                String imageUrl = extractImageFromChoices(output);
                if (imageUrl != null) {
                    return imageUrl;
                }
                JSONArray results = output.getJSONArray("results");
                if (results != null && !results.isEmpty()) {
                    JSONObject first = results.getJSONObject(0);
                    if (first != null) {
                        return first.getString("url");
                    }
                }
                log.warn("文生图任务成功但无结果图 taskId={}", taskId);
                return null;
            }
            if ("FAILED".equalsIgnoreCase(status) || "CANCELED".equalsIgnoreCase(status)) {
                log.warn("文生图任务失败 taskId={} status={}", taskId, status);
                lastFailure.set("图片生成失败：图像生成任务未完成（" + status + "），请稍后重试");
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
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(imageUrl))
                .timeout(Duration.ofSeconds(IMAGE_DOWNLOAD_TIMEOUT_SECONDS))
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