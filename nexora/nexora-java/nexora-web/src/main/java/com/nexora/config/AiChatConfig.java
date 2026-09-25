package com.nexora.config;

import com.nexora.component.AiUsageAdvisor;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class AiChatConfig {

    @Bean
    public ChatClient chatClient(OpenAiChatModel openAiChatModel, AiUsageAdvisor aiUsageAdvisor) {
        // 统一挂用量上报 Advisor：本端所有大模型调用的 token 消耗自动落 ai_usage_record
        return ChatClient.builder(openAiChatModel).defaultAdvisors(aiUsageAdvisor).build();
    }

    @PostConstruct
    public void checkAiEnvironment() {
        checkKey("NEXORA_DEEPSEEK_API_KEY", System.getenv("NEXORA_DEEPSEEK_API_KEY"));
        checkKey("NEXORA_EMBEDDING_API_KEY", System.getenv("NEXORA_EMBEDDING_API_KEY"));
        checkKey("NEXORA_IMAGE_API_KEY", System.getenv("NEXORA_IMAGE_API_KEY"));
    }

    private void checkKey(String name, String value) {
        if (value == null || value.isBlank() || "sk-xxx".equals(value)) {
            log.warn("AI 环境变量缺失或未生效: {}。当前 JVM 未读取到该变量，请完全重启 IntelliJ/IDEA，或在运行配置的 Environment variables 中配置", name);
            return;
        }
        String masked = value.length() > 8
                ? value.substring(0, 4) + "..." + value.substring(value.length() - 4)
                : "***";
        log.info("AI 环境变量已生效: {} ({})", name, masked);
    }
}
