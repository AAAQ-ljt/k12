package com.nexora.component;

import com.alibaba.fastjson2.JSON;
import com.nexora.dto.UserIntentDTO;
import com.nexora.entity.enums.UserIntentEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 意图分析：理科求解类先走关键词规则（省一次模型调用），其余走 LLM 结构化分类，失败兜底 CHAT
 */
@Component
@Slf4j
public class IntentAnalyzerComponent {

    private static final String INTENT_SYSTEM_PROMPT = """
            你是意图分类器。只输出 JSON，不要输出任何解释。
            从以下意图中选择一个：EXPLAIN、RECOMMEND、QUIZ、PICTURE_BOOK、DRAW、ANIMATION、CODING、SCIENCE_SOLVE、PLAN、PROGRESS、CHAT。
            返回格式：{"intent":"EXPLAIN","data":{"knowledgePoint":"冒泡排序"}}。
            无法确定用户意图时使用 CHAT。""";

    /** 概念类问句前缀：命中则不以关键词规则判为理科求解，交给 LLM/RAG 链路 */
    private static final List<String> CONCEPT_PREFIXES = List.of(
            "什么是", "啥是", "什么叫", "这是什么", "介绍一下", "介绍", "解释", "讲解",
            "概念", "原理是", "意思是", "举例", "举个例子", "举例子", "为什么");

    /** 理科求解关键词（数学/物理/化学/生物），含解题句式触发词 */
    private static final List<String> SCIENCE_KEYWORDS = List.of(
            // 数学
            "解方程", "方程", "求导", "导数", "微分", "积分", "求极限", "极限", "不等式",
            "证明", "求解", "三角函数", "对数", "指数", "概率", "排列", "组合数", "几何",
            "勾股", "配方", "函数值", "计算",
            // 物理
            "并联", "串联", "电阻", "电流", "电压", "电功率", "功率", "加速度", "牛顿",
            "浮力", "压强", "做功", "动能", "势能", "机械能", "折射", "反射", "透镜",
            "欧姆", "自由落体", "抛体", "电路",
            // 化学
            "配平", "化学方程式", "化学反应", "摩尔", "浓度", "化合价", "离子", "电解",
            "氧化还原", "中和反应", "沉淀", "溶解度", "化学式",
            // 生物
            "遗传", "基因", "染色体", "显性", "隐性", "杂交", "光合作用", "呼吸作用",
            "细胞分裂", "减数分裂",
            // 解题句式
            "帮我算", "帮我解", "怎么算", "怎么求", "怎么解", "计算一下", "算一下",
            "推导", "这道题", "解题", "做一下这道");

    private final ChatClient chatClient;

    @Value("${spring.ai.openai.chat.options.model:deepseek-v4-flash}")
    private String chatModel;

    public IntentAnalyzerComponent(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public IntentResult analyze(String userMessage, String stage) {
        boolean primaryStage = "PRIMARY_LOW".equalsIgnoreCase(stage) || "PRIMARY_HIGH".equalsIgnoreCase(stage);
        if (matchScienceKeyword(userMessage)) {
            return new IntentResult("SCIENCE_SOLVE", null, 0, 0);
        }
        try {
            String systemPrompt = primaryStage
                    ? INTENT_SYSTEM_PROMPT
                            + " 注意：用户为小学阶段（小低/小高），画面型动画能力不可用：禁止选择 ANIMATION，动画类请求返回 CHAT。"
                    : INTENT_SYSTEM_PROMPT;
            ChatResponse response = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userMessage)
                    .options(OpenAiChatOptions.builder().model(chatModel).build())
                    .call()
                    .chatResponse();
            int promptTokens = 0;
            int completionTokens = 0;
            if (response.getMetadata() != null && response.getMetadata().getUsage() != null) {
                Usage usage = response.getMetadata().getUsage();
                promptTokens = usage.getPromptTokens() == null ? 0 : usage.getPromptTokens();
                completionTokens = usage.getCompletionTokens() == null ? 0 : usage.getCompletionTokens();
            }
            String content = "";
            if (response.getResults() != null && !response.getResults().isEmpty()
                    && response.getResults().get(0).getOutput() != null) {
                content = response.getResults().get(0).getOutput().getText();
            }
            UserIntentDTO dto = parseJson(content);
            String intent = dto == null || dto.getIntent() == null ? "CHAT" : dto.getIntent().trim().toUpperCase();
            if (!UserIntentEnum.isValid(intent)) {
                intent = "CHAT";
            }
            // 硬拦截：小学阶段（小低/小高）不允许动画生成（无动画页面，防止误用绘本提示词）
            if (primaryStage && "ANIMATION".equals(intent)) {
                log.info("小学阶段动画意图降级为 CHAT: {}", userMessage);
                intent = "CHAT";
            }
            return new IntentResult(intent, dto == null ? null : dto.getData(), promptTokens, completionTokens);
        } catch (Exception e) {
            log.warn("意图分析失败，兜底 CHAT", e);
            return new IntentResult("CHAT", null, 0, 0);
        }
    }

    /**
     * 理科求解关键词规则：概念类问句（什么是/解释/为什么等开头）不进入该规则，避免误伤概念查询
     */
    private boolean matchScienceKeyword(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            return false;
        }
        String text = userMessage.trim();
        String lower = text.toLowerCase();
        for (String prefix : CONCEPT_PREFIXES) {
            if (text.startsWith(prefix)) {
                return false;
            }
        }
        return SCIENCE_KEYWORDS.stream().anyMatch(lower::contains);
    }

    private UserIntentDTO parseJson(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }
        String text = content.trim();
        if (text.startsWith("```")) {
            text = text.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "");
        }
        try {
            return JSON.parseObject(text, UserIntentDTO.class);
        } catch (Exception e) {
            log.warn("意图 JSON 解析失败，兜底 CHAT: {}", text);
            return null;
        }
    }

    public record IntentResult(String intent, Map<String, Object> data, int promptTokens, int completionTokens) {
    }
}