package com.nexora.admin.config;

import com.nexora.component.AiUsageAdvisor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    // 向量模型固定使用 spring.ai.openai.embedding.options.model（qwen3.7-text-embedding，1024 维），
    // 不做跨模型兜底：不同模型的向量空间不互通，切换模型必须全量重新向量化（2026-09-29 决策）。
    // 瞬时故障由 Spring AI 自带 RetryTemplate 按重试策略重试，与模型无关。
}
