package com.nexora.component;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 大模型用量上报 Advisor：挂在两端的 ChatClient 上，所有走 ChatClient 的调用（对话 / 意图路由 /
 * 绘本文案 / 动画脚本 / 学习路径 / 出题 / 文档整理 / 模型测试）都自动记一次 token 消耗，
 * 业务代码无需逐个埋点。
 *
 * 设计取舍：
 * - **流式取最后一次用量**：流式响应只有末尾分片带 usage，逐片累加会重复计数，因此只保留最后一次出现的用量；
 * - **上报异常不外抛**：Advisor 位于调用链路上，统计失败必须静默降级，不能影响 AI 回复本身。
 */
@Slf4j
@Component
public class AiUsageAdvisor implements CallAdvisor, StreamAdvisor {

    @Resource
    private AiUsageRecordComponent aiUsageRecordComponent;

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        ChatClientResponse response = chain.nextCall(request);
        recordUsage(response == null ? null : response.chatResponse());
        return response;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        AtomicReference<ChatResponse> lastResponse = new AtomicReference<>();
        return chain.nextStream(request)
                .doOnNext(item -> {
                    if (item != null && item.chatResponse() != null) {
                        lastResponse.set(item.chatResponse());
                    }
                })
                .doOnComplete(() -> recordUsage(lastResponse.get()));
    }

    private void recordUsage(ChatResponse response) {
        try {
            if (response == null || response.getMetadata() == null) {
                return;
            }
            Usage usage = response.getMetadata().getUsage();
            if (usage == null) {
                return;
            }
            aiUsageRecordComponent.recordChatUsage(response.getMetadata().getModel(),
                    usage.getPromptTokens(), usage.getCompletionTokens());
        } catch (Exception e) {
            log.warn("大模型用量上报失败", e);
        }
    }

    @Override
    public String getName() {
        return "AiUsageAdvisor";
    }

    /** 排在内置的模型调用 Advisor 之前，保证能拿到模型返回的原始用量 */
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 10;
    }
}
