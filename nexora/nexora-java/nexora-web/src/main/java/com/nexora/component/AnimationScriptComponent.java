package com.nexora.component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.nexora.entity.enums.StageEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 动画讲解脚本生成组件：LLM 生成分步 SVG 讲解脚本（JSON），逐帧清洗为白名单基础图形后再返回。
 * 产物结构：{ "title": "...", "steps": [ { "title": "...", "explain": "...", "svg": "<svg>...</svg>" } ] }
 * 生成提示词走统一提示词体系（Redis 覆盖 -> prompt_template 表 -> PromptTypeEnum.ANIMATION 默认值）。
 */
@Slf4j
@Component
public class AnimationScriptComponent {

    /** 单步 SVG 最大字符数，超长丢弃该帧（避免异常输出撑爆消息） */
    private static final int MAX_SVG_CHARS = 6000;

    /** 最多保留的步骤数 */
    private static final int MAX_STEPS = 12;

    private static final Pattern SVG_BLOCK = Pattern.compile(
            "<svg\\b[^>]*>.*?</svg>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /** 非法标签块：脚本/样式/嵌入内容/外部引用 */
    private static final Pattern ILLEGAL_TAG_BLOCK = Pattern.compile(
            "<\\s*(script|style|foreignObject|iframe|object|embed|link|meta)(\\b[^>]*)?>.*?</\\s*\\1\\s*>|"
                    + "<\\s*(script|style|foreignObject|iframe|object|embed|link|meta)\\b[^>]*/?>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /** 事件属性与危险引用 */
    private static final Pattern ILLEGAL_ATTR = Pattern.compile(
            "\\s+on\\w+\\s*=\\s*(\"[^\"]*\"|'[^']*'|[^\\s>]+)|"
                    + "\\s+(xlink:)?href\\s*=\\s*(\"[^\"]*\"|'[^']*'|[^\\s>]+)|"
                    + "javascript\\s*:",
            Pattern.CASE_INSENSITIVE);

    /** SVG 允许的标签白名单 */
    private static final List<String> ALLOWED_SVG_TAGS = List.of(
            "svg", "g", "rect", "circle", "ellipse", "line", "polyline", "polygon",
            "path", "text", "tspan", "defs", "linearGradient", "radialGradient", "stop");

    @Resource
    private ChatClient chatClient;

    @Resource
    private PromptTemplateComponent promptTemplateComponent;

    @Value("${spring.ai.openai.chat.options.model:deepseek-v4-flash}")
    private String chatModel;

    /**
     * 生成清洗后的动画脚本；LLM 输出不合法时抛出异常（调用方降级为普通对话）
     */
    public AnimationScript generate(String stage, String concept) {
        String raw = callModel(stage, concept);
        return parseAndSanitize(raw);
    }

    /** 概念解析提示词：内部工具提示词，与 IntentAnalyzerComponent 的意图分类提示词同风格，不走统一模板体系 */
    private static final String CONCEPT_RESOLVE_SYSTEM_PROMPT = """
            你是动画概念提取器。根据"最近对话"和"学生最新指令"，确定动画讲解要呈现的具体概念或主题。
            规则：
            1. 指令已明确给出概念（如"生成冒泡排序的动画讲解"），直接返回该概念本身；
            2. 指令是指代型（如"把刚才/刚刚/上面讲的内容做成动画"），从最近对话中找最近一次讲解的核心概念，
               用 10~40 字概括，可带教材或章节限定（如"公有制为主体 多种所有制经济共同发展"）；
            3. 指代不明确且对话里有多个主题时，取最近一次讲解的主题；
            4. 只输出概念文本本身，不要解释、不要引号、不要标点结尾。""";

    /**
     * 动画概念解析：把"把刚才讲的内容做成动画"这类指代型指令结合最近对话解析成具体概念；
     * 解析失败兜底返回原始指令，保证旧链路始终可用
     */
    public String resolveConcept(String stage, String userMessage, String recentTranscript) {
        try {
            String userPrompt = "【最近对话】\n" + (recentTranscript == null || recentTranscript.isBlank()
                    ? "（无）" : recentTranscript.stripTrailing())
                    + "\n\n【学生最新指令】" + userMessage
                    + "\n\n请输出本次动画要讲解的概念（学生学段：" + stageDesc(stage) + "）。只输出概念文本。";
            String content = chatClient.prompt()
                    .system(CONCEPT_RESOLVE_SYSTEM_PROMPT)
                    .user(userPrompt)
                    .options(OpenAiChatOptions.builder().model(chatModel).build())
                    .call()
                    .content();
            String concept = normalizeConcept(content);
            if (concept == null) {
                log.warn("动画概念解析结果为空，回退原始指令: {}", userMessage);
                return userMessage;
            }
            log.info("动画概念解析完成: {} -> {}", userMessage, concept);
            return concept;
        } catch (Exception e) {
            log.warn("动画概念解析失败，回退原始指令: {}", userMessage, e);
            return userMessage;
        }
    }

    /** 概念清洗：去代码块标记与首尾引号，取首行，限长；清洗后为空视为解析失败 */
    private String normalizeConcept(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }
        String text = content.trim();
        if (text.startsWith("```")) {
            text = text.replaceFirst("^```(?:json|text)?\\s*", "").replaceFirst("\\s*```$", "");
        }
        text = text.replaceAll("^[「『\"']+|[」』\"']+$", "").trim();
        int lineEnd = text.indexOf('\n');
        if (lineEnd >= 0) {
            text = text.substring(0, lineEnd).trim();
        }
        if (text.length() > 80) {
            text = text.substring(0, 80).trim();
        }
        return text.isBlank() ? null : text;
    }

    private String callModel(String stage, String concept) {
        String systemPrompt = promptTemplateComponent.resolvePrompt(stage, "ANIMATION");
        String userPrompt = "请为概念「" + (concept == null ? "" : concept)
                + "」生成动画讲解脚本（学生学段：" + stageDesc(stage) + "）。只输出 JSON。";
        try {
            return chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .options(OpenAiChatOptions.builder().model(chatModel).build())
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("动画脚本生成调用失败", e);
            throw new RuntimeException("动画生成失败");
        }
    }

    private AnimationScript parseAndSanitize(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new RuntimeException("动画脚本为空");
        }
        String text = raw.trim();
        if (text.startsWith("```")) {
            text = text.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "");
        }
        JSONObject root;
        try {
            root = JSON.parseObject(text);
        } catch (Exception e) {
            // 模型输出可能夹带说明文字（GLM 等强制思考模型尤甚），先截取首个平衡的 JSON 对象再解析
            String candidate = extractJsonObject(text);
            if (candidate == null) {
                log.warn("动画脚本输出未找到 JSON 对象，原始输出前 500 字符：{}",
                        text.substring(0, Math.min(500, text.length())));
                throw new RuntimeException("动画脚本解析失败");
            }
            try {
                root = JSON.parseObject(candidate);
            } catch (Exception e2) {
                log.warn("动画脚本 JSON 子串解析失败，原始输出前 500 字符：{}",
                        text.substring(0, Math.min(500, text.length())));
                throw new RuntimeException("动画脚本解析失败");
            }
        }
        if (root == null) {
            throw new RuntimeException("动画脚本解析失败");
        }
        String title = root.getString("title");
        JSONArray steps = root.getJSONArray("steps");
        if (steps == null || steps.isEmpty()) {
            throw new RuntimeException("动画脚本缺少步骤");
        }
        List<AnimationStep> safeSteps = new ArrayList<>();
        for (int i = 0; i < steps.size() && i < MAX_STEPS; i++) {
            JSONObject step = steps.getJSONObject(i);
            if (step == null) {
                continue;
            }
            String stepTitle = step.getString("title");
            String explain = step.getString("explain");
            String svg = sanitizeSvg(step.getString("svg"));
            if ((stepTitle == null || stepTitle.isBlank()) && (explain == null || explain.isBlank())) {
                continue;
            }
            safeSteps.add(new AnimationStep(
                    stepTitle == null ? "" : stepTitle,
                    explain == null ? "" : explain,
                    svg));
        }
        if (safeSteps.isEmpty()) {
            throw new RuntimeException("动画脚本步骤为空");
        }
        return new AnimationScript(title == null ? "" : title, safeSteps);
    }

    /**
     * 从模型原始输出中截取首个括号平衡的 JSON 对象子串（跳过字符串字面量内的引号与转义），找不到返回 null。
     * 用于兼容 GLM 等模型在 JSON 前后夹带说明文字/思考残留的输出。
     */
    private String extractJsonObject(String text) {
        int start = text.indexOf('{');
        while (start >= 0) {
            int depth = 0;
            boolean inString = false;
            boolean escaped = false;
            for (int i = start; i < text.length(); i++) {
                char c = text.charAt(i);
                if (inString) {
                    if (escaped) {
                        escaped = false;
                    } else if (c == '\\') {
                        escaped = true;
                    } else if (c == '"') {
                        inString = false;
                    }
                    continue;
                }
                if (c == '"') {
                    inString = true;
                } else if (c == '{') {
                    depth++;
                } else if (c == '}') {
                    depth--;
                    if (depth == 0) {
                        return text.substring(start, i + 1);
                    }
                }
            }
            start = text.indexOf('{', start + 1);
        }
        return null;
    }

    /**
     * SVG 白名单清洗：仅保留基础图形标签与安全属性，剔除脚本/样式/事件/外链
     */
    public static String sanitizeSvg(String rawSvg) {
        if (rawSvg == null || rawSvg.isBlank()) {
            return "";
        }
        Matcher matcher = SVG_BLOCK.matcher(rawSvg);
        if (!matcher.find()) {
            return "";
        }
        String svg = matcher.group();
        svg = ILLEGAL_TAG_BLOCK.matcher(svg).replaceAll("");
        svg = ILLEGAL_ATTR.matcher(svg).replaceAll("");
        // 标签白名单：非白名单标签整体删除（含开闭标签）
        Pattern tagPattern = Pattern.compile("<(/)?\\s*([a-zA-Z][a-zA-Z0-9]*)(\\b[^>]*)?(/)?>");
        StringBuilder cleaned = new StringBuilder();
        Matcher tagMatcher = tagPattern.matcher(svg);
        int last = 0;
        while (tagMatcher.find()) {
            String tagName = tagMatcher.group(2).toLowerCase();
            boolean isAllowed = ALLOWED_SVG_TAGS.contains(tagName);
            if (tagMatcher.start() > last) {
                cleaned.append(svg, last, tagMatcher.start());
            }
            if (isAllowed) {
                cleaned.append(tagMatcher.group());
            }
            last = tagMatcher.end();
        }
        cleaned.append(svg, last, svg.length());
        String result = cleaned.toString();
        if (result.length() > MAX_SVG_CHARS) {
            // 超长动画帧丢弃，保证消息体积可控
            return "";
        }
        return result;
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

    public record AnimationStep(String title, String explain, String svg) {
    }

    public record AnimationScript(String title, List<AnimationStep> steps) {
        public String toJson() {
            JSONObject root = new JSONObject();
            root.put("title", title);
            JSONArray array = new JSONArray();
            for (AnimationStep step : steps) {
                JSONObject item = new JSONObject();
                item.put("title", step.title());
                item.put("explain", step.explain());
                item.put("svg", step.svg());
                array.add(item);
            }
            root.put("steps", array);
            return root.toJSONString();
        }
    }
}