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
            2. 用清晰的一级/二级标题层级组织内容，开头先写一段「摘要」（3-5 句话概括核心知识）；
            3. 核心概念、公式（用 $...$ 或 $$...$$ 的 LaTeX）、代码块、要点列表尽量原样保留，不改写事实；
            4. 资料包含多个主题时按主题分节，每节下用要点列表展开；
            5. 只输出整理后的 Markdown 正文，不要输出任何解释、前言或"以下是整理结果"之类的说明。""";

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
        List<String> segments = splitSegments(text, MAX_SEGMENT_CHARS);
        List<String> results = new ArrayList<>();
        for (int i = 0; i < segments.size(); i++) {
            String userPrompt;
            if (segments.size() == 1) {
                userPrompt = "资料标题：" + (title == null ? "" : title)
                        + "\n\n以下是需要整理的原始资料：\n\n" + segments.get(i);
            } else {
                userPrompt = "资料标题：" + (title == null ? "" : title)
                        + "\n资料较长，以下是第 " + (i + 1) + "/" + segments.size()
                        + " 部分，请只整理这一部分：\n\n" + segments.get(i);
            }
            String content = callModel(stageDesc, userPrompt);
            if (!StringTools.isEmpty(content)) {
                results.add(content);
            }
        }
        return String.join("\n\n---\n\n", results).trim();
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

    private List<String> splitSegments(String text, int maxChars) {
        List<String> segments = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return segments;
        }
        if (text.length() <= maxChars) {
            segments.add(text);
            return segments;
        }
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + maxChars, text.length());
            if (end < text.length()) {
                int newline = text.lastIndexOf('\n', end);
                if (newline > start + maxChars / 2) {
                    end = newline;
                }
            }
            segments.add(text.substring(start, end));
            start = end;
        }
        return segments;
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