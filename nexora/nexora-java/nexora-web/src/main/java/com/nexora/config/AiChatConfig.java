package com.nexora.config;

import com.nexora.component.ChatProvider;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class AiChatConfig {

    /**
     * 全局 ChatClient 跟随「对话供应商」路由（system_config 的 AI_MODEL.chat_provider）。
     *
     * 固定注入本 bean 的调用方（意图识别 / 出题 / 动画脚本 / 绘本故事 / 学习路径生成 / 知识页整理，
     * 以及 common 的 AiStructureComponent）此前固定走 spring.ai.openai.chat.*（DeepSeek 直连），
     * 与管理端可切换的对话供应商**割裂**：2026-10-07 DeepSeek 账号欠费时，这些功能全部 402，
     * 且意图识别失败会把「出题」兜底成 CHAT，表现为"AI 把题目写成一段文字、没有答题卡片"。
     * 改为跟随路由后，管理端切换供应商即对全部调用方生效。
     *
     * provider 的客户端已自带用量上报 Advisor（AiUsageAdvisor）；OpenCode Go 还自带网关必需的
     * x-opencode-session 请求头。
     *
     * 注意：bean 在**启动时定型**，运行时切换供应商后这些固定调用方仍需重启本服务（对话主链路是
     * 每次调用实时取路由，不受影响）。
     */
    @Bean
    public ChatClient chatClient(ChatProvider chatProvider) {
        return chatProvider.chatClient();
    }

    // 向量模型固定使用 spring.ai.openai.embedding.options.model（qwen3.7-text-embedding，1024 维），
    // 不做跨模型兜底：不同模型的向量空间不互通，切换模型必须全量重新向量化（2026-09-29 决策）。
    // 瞬时故障由 Spring AI 自带 RetryTemplate 按重试策略重试，与模型无关。

    @PostConstruct
    public void checkAiEnvironment() {
        checkKey("NEXORA_DEEPSEEK_API_KEY", System.getenv("NEXORA_DEEPSEEK_API_KEY"));
        checkKey("NEXORA_EMBEDDING_API_KEY", System.getenv("NEXORA_EMBEDDING_API_KEY"));
        checkKey("NEXORA_IMAGE_API_KEY", System.getenv("NEXORA_IMAGE_API_KEY"));
        // 仅当对话供应商切到 opencode-go 时才必需，缺失只在启动日志预警，不影响启动
        checkKey("NEXORA_OPENCODE_GO_API_KEY", System.getenv("NEXORA_OPENCODE_GO_API_KEY"));
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
