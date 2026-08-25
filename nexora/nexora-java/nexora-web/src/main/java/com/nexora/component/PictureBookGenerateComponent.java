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
 * 绘本生成组件：LLM 生成故事分页文案（JSON），逐页通过 ImageProvider 生成插图并下载到本地。
 * 产物结构（存 resource_info.ext_json）：{ "type":"PICTURE_BOOK", "pages":[ { "text":"...", "imageFile":"相对路径" } ] }
 * 图像生成失败该页降级为纯文字（imageFile 为空），不阻断整个绘本。
 */
@Slf4j
@Component
public class PictureBookGenerateComponent {

    private static final int MAX_PAGES = 8;
    /** 失败图片下载超时（生成后的结果图较大，单独放宽） */
    private static final int IMAGE_DOWNLOAD_TIMEOUT_SECONDS = 60;

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
    private final ImageProvider imageProvider;

    @Value("${project.folder}")
    private String projectFolder;

    @Value("${resource.file-dir}")
    private String resourceFileDir;

    public PictureBookGenerateComponent(ChatClient chatClient, ImageProvider imageProvider) {
        this.chatClient = chatClient;
        this.imageProvider = imageProvider;
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

    /** 最近一次插图失败原因（供外部记录；并发模式下取最后一次写入值） */
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
            ImageGenerateResult result = imageProvider.generate(prompt);
            if (!result.success()) {
                lastFailure.set(result.errorMessage() == null ? "图片生成失败" : result.errorMessage());
                return null;
            }
            return downloadImage(email, result.imageUrl());
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

    public record StoryScript(String title, List<String> pages) {
    }
}
