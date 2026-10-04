package com.nexora.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * 对话模型供应商路由，与 {@link ImageProviderRouter} 同一套设计：
 * 按 system_config 的 AI_MODEL.chat_provider 运行时切换（改库即生效，无需重启），
 * 表里未配置时回落启动配置 project.ai.chat.provider，仍然非法时兜底 deepseek。
 *
 * 两个具体供应商（deepseek / opencode-go）全部常驻注册，
 * 本路由作为 ChatProvider 的主实现（@Primary）供业务注入，各方法转发到当前供应商。
 */
@Slf4j
@Component
@Primary
public class ChatProviderRouter implements ChatProvider {

    private final DeepSeekChatProvider deepSeekChatProvider;
    private final OpenCodeGoChatProvider openCodeGoChatProvider;
    private final SystemConfigComponent systemConfigComponent;

    /** 启动配置默认供应商（表里无覆盖时生效） */
    @Value("${project.ai.chat.provider:deepseek}")
    private String defaultProvider;

    public ChatProviderRouter(DeepSeekChatProvider deepSeekChatProvider,
                              OpenCodeGoChatProvider openCodeGoChatProvider,
                              SystemConfigComponent systemConfigComponent) {
        this.deepSeekChatProvider = deepSeekChatProvider;
        this.openCodeGoChatProvider = openCodeGoChatProvider;
        this.systemConfigComponent = systemConfigComponent;
    }

    @Override
    public String code() {
        return current().code();
    }

    @Override
    public String label() {
        return current().label();
    }

    @Override
    public ChatProvider current() {
        String code = SystemConfigComponent.normalizeChatProvider(
                systemConfigComponent.getChatProviderValue(defaultProvider));
        switch (code) {
            case SystemConfigComponent.PROVIDER_OPENCODE_GO:
                return openCodeGoChatProvider;
            case SystemConfigComponent.PROVIDER_DEEPSEEK:
            default:
                return deepSeekChatProvider;
        }
    }

    @Override
    public ChatClient chatClient() {
        return current().chatClient();
    }

    @Override
    public String textModel() {
        return current().textModel();
    }

    @Override
    public String visionModel() {
        return current().visionModel();
    }

    @Override
    public String reasoningEffort(boolean withImage) {
        return current().reasoningEffort(withImage);
    }

    @Override
    public Boolean parallelToolCalls() {
        return current().parallelToolCalls();
    }
}
