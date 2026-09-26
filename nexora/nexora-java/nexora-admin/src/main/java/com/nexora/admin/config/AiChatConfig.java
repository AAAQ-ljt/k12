package com.nexora.admin.config;

import com.nexora.component.AiUsageAdvisor;
import com.nexora.component.FallbackEmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Arrays;
import java.util.List;

/**
 * 管理端 AI 能力：出题、解析等统一收敛到 ChatClient。
 */
@Configuration
@Slf4j
public class AiChatConfig {

    @Bean
    public ChatClient chatClient(OpenAiChatModel openAiChatModel, AiUsageAdvisor aiUsageAdvisor) {
        // 统一挂用量上报 Advisor：本端所有大模型调用的 token 消耗自动落 ai_usage_record
        return ChatClient.builder(openAiChatModel).defaultAdvisors(aiUsageAdvisor).build();
    }

    /**
     * 向量模型兜底链：主模型（spring.ai.openai.embedding.options.model）配额用完或模型不可用时，
     * 自动降级到 fallback-models 列表（逗号分隔，按顺序尝试）。
     * 包装为 @Primary 后，向量库与所有按类型注入 EmbeddingModel 的组件自动走兜底，业务零改动。
     */
    @Bean
    @Primary
    public EmbeddingModel embeddingModel(OpenAiEmbeddingModel openAiEmbeddingModel,
            @Value("${project.ai.embedding.fallback-models:}") String fallbackModels,
            @Value("${spring.ai.openai.embedding.options.dimensions:1024}") int dimensions) {
        List<String> models = Arrays.stream(fallbackModels.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        if (models.isEmpty()) {
            return openAiEmbeddingModel;
        }
        log.info("向量模型兜底链已启用: 主模型 -> {} (dimensions={})", models, dimensions);
        return new FallbackEmbeddingModel(openAiEmbeddingModel, models, dimensions);
    }
}
