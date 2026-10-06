package com.nexora.component;

import com.nexora.entity.enums.StageEnum;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 结构化整理组件（公共能力，两端共用）：
 * 官方库「AI 文档整理」（管理端确认后向量化）与学生个人 Wiki 生成（用户确认后向量化）共用此整理链路。
 * 将原始提取文本整理为结构化 Markdown（标题层级 + 摘要 + 要点），超长文本分段整理后拼接。
 * 模型使用当前端配置的默认对话模型（严禁硬编码）。
 */
@Slf4j
@Component
public class AiStructureComponent {

    /** 单次整理的最大输入字符数，超过则分段处理避免截断丢内容 */
    private static final int MAX_SEGMENT_CHARS = 6000;

    private static final String SYSTEM_PROMPT = """
            你是 K12 人工智能通识课的「AI 结构化整理」助手。
            请把用户提供的原始学习资料整理成结构化的 Markdown 知识页，要求：
            1. 保留知识点主体，去掉前言、广告、重复与噪声内容；
            2. **正文不要输出一级标题（`#`），最高用二级标题 `##`，小节用 `###`**——文档标题由系统统一添加；
            3. 核心概念、公式（用 $...$ 或 $$...$$ 的 LaTeX）、代码块、要点列表尽量原样保留，不改写事实；
            4. 资料包含多个主题时按主题分节，每节下用要点列表展开；
            5. 只输出整理后的 Markdown 正文，不要输出任何解释、前言或"以下是整理结果"之类的说明，也不要输出分隔线（`---`）。""";

    /** 大纲提示词：长文分段前先让模型产出全文档结构，供各段对齐编号与章节归属 */
    private static final String OUTLINE_PROMPT = """
            以下是资料《%s》的开头部分。请只输出这份资料的**结构大纲**：按顺序列出单元/章节标题，
            每行一条（保留资料本身的编号与名称，如「第一单元 5以内数的认识和加、减法」），
            最多 12 条，不要编号之外的解释、不要整理正文内容。""";

    /** 段间重叠字数：避免在切分点附近断章（下一段回带上一段尾部） */
    private static final int SEGMENT_OVERLAP_CHARS = 200;

    /** 结构边界识别：标题行 / 单元·章·节·课·部分 / Unit·Part·Chapter / 数字序号行 */
    private static final java.util.regex.Pattern STRUCTURE_LINE = java.util.regex.Pattern.compile(
            "^\\s*(#{1,6}\\s|第[一二三四五六七八九十百0-9]+\\s*[单课章节元]|Unit\\b|Part\\b|Chapter\\b|[0-9]{1,3}\\s*[.、])",
            java.util.regex.Pattern.MULTILINE);

    @Resource
    private ChatClient chatClient;

    /**
     * 生成结构化 Markdown 知识页
     *
     * @param stage 学段编码（StageEnum），用于提示词适配
     * @param title 资料标题
     * @param text  原始提取文本
     * @return 结构化 Markdown（空文本返回空串）
     */
    public String generateStructure(String stage, String title, String text) {
        if (StringTools.isEmpty(text)) {
            return "";
        }
        String stageDesc = stageDesc(stage);
        String safeTitle = StringTools.isEmpty(title) ? "学习资料整理" : title.trim();
        List<Segment> segments = splitSegments(text, MAX_SEGMENT_CHARS);
        // 第一遍：长文先产出全文档大纲，后续每段据此对齐单元编号、避免章节重复
        String outline = "";
        if (segments.size() > 1) {
            outline = callModel(stageDesc, String.format(OUTLINE_PROMPT, safeTitle)
                    + "\n\n开头部分原文：\n\n" + joinHead(segments, 2));
            outline = outline == null ? "" : outline.trim();
        }
        List<String> results = new ArrayList<>();
        for (int i = 0; i < segments.size(); i++) {
            String content = callModel(stageDesc, buildSegmentPrompt(safeTitle, segments.size(), i, segments.get(i), outline));
            if (!StringTools.isEmpty(content)) {
                results.add(cleanSegment(content));
            }
        }
        if (results.isEmpty()) {
            return "";
        }
        // 文档标题由代码统一添加（模型被要求不输出 # 一级标题）；段间不再插 --- 分隔线
        return ("# " + safeTitle + "\n\n" + String.join("\n\n", results)).trim();
    }

    /** 组装单段整理提示词：第 1 段负责摘要与内容框架，其余段只整理本段覆盖的章节 */
    private String buildSegmentPrompt(String title, int total, int index, Segment segment, String outline) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("资料标题：").append(title).append("\n");
        if (total == 1) {
            prompt.append("\n请在正文开头先写一段【摘要】（3-5 句话概括核心知识），再写一个【内容框架】")
                    .append("（用列表列出全部单元/章节），然后分节整理正文。\n");
        } else if (index == 0) {
            prompt.append("\n这是全文的第 1/").append(total).append(" 部分，**只有你需要写【摘要】与【内容框架】**")
                    .append("（其余部分由别的调用处理，你不要写其它部分的正文）。\n");
        } else {
            prompt.append("\n这是全文的第 ").append(index + 1).append("/").append(total)
                    .append(" 部分：**不要写摘要、不要写内容框架**，只整理本部分实际覆盖的章节；")
                    .append("标题从 `##` 开始，并沿用下方大纲里的单元编号与名称，不要重复前面部分已整理过的章节。\n");
        }
        if (!StringTools.isEmpty(outline)) {
            prompt.append("\n【本文结构大纲（供你对齐编号与章节归属）】\n").append(outline).append("\n");
        }
        if (!StringTools.isEmpty(segment.overlap())) {
            prompt.append("\n【上一部分结尾的重叠内容：仅供你衔接上下文，**不要整理、不要输出这一段的任何内容**】\n")
                    .append(segment.overlap())
                    .append("\n【重叠内容结束——请从下面的正文开始整理，若正文开头是某个章节的延续，")
                    .append("沿用该章节标题继续补充即可，不要重新整理已出现的要点】\n");
        }
        prompt.append("\n以下是本部分需要整理的正文：\n\n").append(segment.body());
        return prompt.toString();
    }

    /**
     * 规范化单段输出：把模型仍写出的 `#` 一级标题降级为 `##`（文档标题由代码统一添加），
     * 并去掉段落首尾可能出现的分隔线与空行。
     */
    private String cleanSegment(String content) {
        String cleaned = content.replaceAll("(?m)^#\\s+", "## ");
        cleaned = cleaned.replaceAll("(?m)^\\s*(-{3,}|\\*{3,})\\s*$", "");
        return cleaned.trim();
    }

    /** 取前 n 段的正文（用于大纲提示词的「开头部分」） */
    private String joinHead(List<Segment> segments, int count) {
        int limit = Math.min(count, segments.size());
        return segments.subList(0, limit).stream().map(Segment::body).collect(java.util.stream.Collectors.joining("\n\n"));
    }

    private String callModel(String stageDesc, String userPrompt) {
        try {
            return chatClient.prompt()
                    .system(SYSTEM_PROMPT + "\n学生当前学段：" + stageDesc)
                    .user(userPrompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("AI 结构化整理调用失败", e);
            throw new RuntimeException("AI 整理失败，请稍后重试");
        }
    }

    /** 分段结果：overlap = 上一段结尾的重叠内容（仅供衔接，模型不得整理、不得输出）；body = 本段需要整理的正文 */
    private record Segment(String overlap, String body) {
    }

    /**
     * 长文分段：优先在**结构边界**（标题行 / 第X单元·章·节·课 / 数字序号行）处切，
     * 找不到边界时退化为按行切；段间保留 {@link #SEGMENT_OVERLAP_CHARS} 字重叠，
     * 重叠部分在提示词里被显式标注为「不要整理」（否则模型会把重叠段重新整理一遍，造成章节重复）。
     */
    private List<Segment> splitSegments(String text, int maxChars) {
        List<Segment> segments = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return segments;
        }
        if (text.length() <= maxChars) {
            segments.add(new Segment("", text));
            return segments;
        }
        int start = 0;
        int overlapEnd = 0;
        while (start < text.length()) {
            int end = Math.min(start + maxChars, text.length());
            if (end < text.length()) {
                end = findBreakPoint(text, start, end, maxChars);
            }
            String overlap = start < overlapEnd ? text.substring(start, Math.min(overlapEnd, end)).trim() : "";
            String body = text.substring(Math.max(start, overlapEnd), end).trim();
            if (!body.isEmpty()) {
                segments.add(new Segment(overlap, body));
            }
            if (end >= text.length()) {
                break;
            }
            // 下一段回带一段重叠内容（对齐到行首）：跨段边界处不断章、不丢上下文
            int overlapStart = Math.max(start + 1, end - SEGMENT_OVERLAP_CHARS);
            int lineStart = text.lastIndexOf('\n', overlapStart);
            int nextStart = lineStart > start ? lineStart + 1 : end;
            overlapEnd = end;
            start = nextStart;
        }
        return segments;
    }

    /** 在 [start + maxChars/2, end] 内向前找最近的结构边界行；找不到则退回最近换行处 */
    private int findBreakPoint(String text, int start, int end, int maxChars) {
        int lowerBound = start + maxChars / 2;
        int cursor = end;
        while (cursor > lowerBound) {
            int lineStart = text.lastIndexOf('\n', cursor - 1);
            if (lineStart < lowerBound) {
                break;
            }
            String line = text.substring(lineStart + 1, Math.min(cursor, text.length()));
            if (STRUCTURE_LINE.matcher(line).find()) {
                return lineStart + 1;
            }
            cursor = lineStart;
        }
        int newline = text.lastIndexOf('\n', end);
        return newline > lowerBound ? newline : end;
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
}