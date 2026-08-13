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

import java.util.Map;

/**
 * 轻量意图分析：结构化解析用户消息，失败兜底 CHAT
 */
@Component
@Slf4j
public class IntentAnalyzerComponent {

    private static final String INTENT_SYSTEM_PROMPT = """
            你是意图分类器。只输出 JSON，不要输出任何解释。
            从以下意图中选择一个：EXPLAIN、RECOMMEND、QUIZ、PICTURE_BOOK、DRAW、ANIMATION、CODING、PLAN、PROGRESS、CHAT。
            返回格式：{"intent":"EXPLAIN","data":{"knowledgePoint":"冒泡排序"}}。
            无法确定用户意图时使用 CHAT。""";

    private final ChatClient chatClient;

    @Value("${spring.ai.openai.chat.options.model:deepseek-v4-flash}")
    private String chatModel;

    public IntentAnalyzerComponent(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public IntentResult analyze(String userMessage) {
        try {
            ChatResponse response = chatClient.prompt()
                    .system(INTENT_SYSTEM_PROMPT)
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
            return new IntentResult(intent, dto == null ? null : dto.getData(), promptTokens, completionTokens);
        } catch (Exception e) {
            log.warn("意图分析失败，兜底 CHAT", e);
            return new IntentResult("CHAT", null, 0, 0);
        }
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
