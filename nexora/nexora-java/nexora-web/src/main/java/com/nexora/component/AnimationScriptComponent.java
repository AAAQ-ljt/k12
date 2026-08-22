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
 */
@Slf4j
@Component
public class AnimationScriptComponent {

    /** 单步 SVG 最大字符数，超长丢弃该帧（避免异常输出撑爆消息） */
    private static final int MAX_SVG_CHARS = 6000;

    /** 最多保留的步骤数 */
    private static final int MAX_STEPS = 12;

    private static final String SYSTEM_PROMPT = """
            你是 K12 人工智能通识课的「动画讲解导演」。学生当前学段：%s。
            针对用户给出的概念，设计一张分步讲解动画脚本，只输出一个 JSON 对象，不要输出任何解释或 Markdown 代码块标记。
            JSON 结构：
            {
              "title": "动画标题（简短）",
              "steps": [
                {
                  "title": "步骤标题（简短，如：第一步：比较相邻元素）",
                  "explain": "该步的讲解文字（2-4 句，适合该学段，口语化）",
                  "svg": "自包含的 SVG 画面（宽 640 高 400，仅基础图形 text/rect/circle/ellipse/line/polyline/polygon/path，白色或浅色背景，深色文字，用于示意该步骤的画面）"
                }
              ]
            }
            要求：
            1. steps 为 4-8 步，逐步递进，最后一步总结；
            2. svg 必须是一个完整的 <svg xmlns="http://www.w3.org/2000/svg" width="640" height="400">...</svg> 字符串；
            3. svg 只使用基础图形与 <text> 文字，禁止 script、style、foreignObject、iframe、动画标签、外部链接；
            4. 画面用简单图形示意即可（如数组格子、小球、阶梯、箭头），配合文字说明；
            5. explain 面向 %s 学段学生，语言生动易懂。""";

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

    @Value("${spring.ai.openai.chat.options.model:deepseek-v4-flash}")
    private String chatModel;

    /**
     * 生成清洗后的动画脚本；LLM 输出不合法时抛出异常（调用方降级为普通对话）
     */
    public AnimationScript generate(String stage, String concept) {
        String stageDesc = stageDesc(stage);
        String raw = callModel(stage, concept, stageDesc);
        return parseAndSanitize(raw);
    }

    private String callModel(String stage, String concept, String stageDesc) {
        String systemPrompt = String.format(SYSTEM_PROMPT, stageDesc, stageDesc);
        String userPrompt = "请为概念「" + (concept == null ? "" : concept)
                + "」生成动画讲解脚本（学生学段：" + stageDesc + "）。只输出 JSON。";
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
            throw new RuntimeException("动画脚本解析失败");
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