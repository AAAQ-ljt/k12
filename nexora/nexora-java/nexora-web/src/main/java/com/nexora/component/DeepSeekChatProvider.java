package com.nexora.component;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * DeepSeek 官方直连供应商（默认）。
 *
 * 直接复用 spring.ai.openai.chat.* 自动装配出来的 ChatClient，因此本供应商生效时的行为
 * 与引入供应商体系之前完全一致：同一个客户端、同一组模型名与同一份 reasoning-effort 配置。
 */
@Slf4j
@Component
public class DeepSeekChatProvider implements ChatProvider {

    @Resource
    private ChatClient chatClient;

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
        return chatClient;
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
