package com.nexora.component;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * DeepSeek 官方直连供应商（默认）。
 *
 * 基于 spring.ai.openai.chat.* 自动装配出来的**模型**自建 ChatClient（同一组模型名与同一份
 * reasoning-effort 配置），并挂上与全局一致的用量上报 Advisor —— 行为与引入供应商体系之前一致。
 *
 * 注意：2026-10-07 起全局 ChatClient bean 改为跟随本路由（见 AiChatConfig），因此这里**不能**再注入
 * 全局 ChatClient bean，否则会形成「bean ← 路由 ← 本供应商 ← bean」的循环依赖。
 */
@Slf4j
@Component
public class DeepSeekChatProvider implements ChatProvider {

    @Resource
    private OpenAiChatModel openAiChatModel;

    @Resource
    private AiUsageAdvisor aiUsageAdvisor;

    private volatile ChatClient chatClient;

    @Value("${spring.ai.openai.chat.options.model:deepseek-v4-flash}")
    private String chatModel;

    @Value("${project.ai.vision-model:deepseek-v4-flash-vision-exp}")
    private String visionModel;

    @Value("${spring.ai.openai.chat.options.reasoning-effort:high}")
    private String reasoningEffort;

    @Override
    public String code() {
        return SystemConfigComponent.PROVIDER_DEEPSEEK;
    }

    @Override
    public String label() {
        return "DeepSeek 官方直连";
    }

    @Override
    public ChatProvider current() {
        return this;
    }

    @Override
    public ChatClient chatClient() {
        ChatClient client = chatClient;
        if (client == null) {
            synchronized (this) {
                client = chatClient;
                if (client == null) {
                    client = ChatClient.builder(openAiChatModel).defaultAdvisors(aiUsageAdvisor).build();
                    chatClient = client;
                }
            }
        }
        return client;
    }

    @Override
    public String textModel() {
        return chatModel;
    }

    @Override
    public String visionModel() {
        return visionModel;
    }

    /** DeepSeek 的视觉模型不接受 reasoning-effort（带了会 400），因此带图请求一律省略该参数 */
    @Override
    public String reasoningEffort(boolean withImage) {
        return withImage ? null : reasoningEffort;
    }
}
