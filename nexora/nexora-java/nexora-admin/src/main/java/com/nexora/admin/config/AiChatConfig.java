package com.nexora.admin.config;

import com.nexora.component.ChatProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 管理端 AI 能力：出题、解析、学习报告、模型验证等统一收敛到 ChatClient。
 */
@Configuration
@Slf4j
public class AiChatConfig {

    /**
     * 全局 ChatClient 跟随「对话供应商」路由（system_config 的 AI_MODEL.chat_provider）。
     *
     * 管理端此前固定走 spring.ai.openai.chat.*（DeepSeek 直连），学生端早已支持切换——两端不一致：
     * 2026-10-07 DeepSeek 账号欠费时，管理端 AI 出题 / 课时测验出题 / 学习报告 / 模型验证全部 402，
     * 而学生端对话仍正常（走 OpenCode Go）。现改为与学生端共用同一套 ChatProvider 路由
     * （供应商组件已迁到 nexora-common），管理端也随切换生效。
     *
     * provider 的客户端已自带用量上报 Advisor（token 消耗仍落 ai_usage_record）。
     * 注意：bean 在**启动时定型**，运行时切换供应商后管理端需重启本服务。
     */
    @Bean
    public ChatClient chatClient(ChatProvider chatProvider) {
        return chatProvider.chatClient();
    }

    // 向量模型固定使用 spring.ai.openai.embedding.options.model（qwen3.7-text-embedding，1024 维），
    // 不做跨模型兜底：不同模型的向量空间不互通，切换模型必须全量重新向量化（2026-09-29 决策）。
    // 瞬时故障由 Spring AI 自带 RetryTemplate 按重试策略重试，与模型无关。
}
