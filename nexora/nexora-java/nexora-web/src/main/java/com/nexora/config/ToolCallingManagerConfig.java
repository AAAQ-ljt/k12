package com.nexora.config;

import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.execution.ToolExecutionExceptionProcessor;
import org.springframework.ai.tool.resolution.ToolCallbackResolver;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 工具调用管理器：屏蔽 tool 结果消息上的非标准 name 字段（2026-10-05）。
 *
 * <p>背景：Spring AI 1.1.x 的 OpenAiChatModel 在把工具执行结果回传给模型时，会给 role=tool 的消息
 * 带上顶层 name（取值 ToolResponse.name()，即工具函数名）。而 OpenAI 规范里 role=tool 只有
 * role / content / tool_call_id 三个字段，name 是 user / assistant 等角色的字段。OpenCode Go 网关的
 * 上游做严格校验，遇到该字段直接 400：
 * {@code invalid_request_error ... [unsupported_parameter] messages[N]: "name" is not supported by this endpoint}。
 *
 * <p>现象：普通对话正常，一旦模型真的调用 MCP 工具整轮就失败；且带该字段的是同一轮内的第二次请求
 * （工具结果回传），所以报错下标总落在本轮 tool 消息上（历史条数 + system + 当前提问 + assistant.tool_calls 之后）。
 *
 * <p>处理方式：委托默认管理器执行工具，再把回传历史里的 ToolResponseMessage 重建、把 name 置空。
 * OpenAiApi$ChatCompletionMessage 带 @JsonInclude(NON_NULL)，name 为 null 时不会出现在请求体里；
 * role=tool 的 tool_call_id 保留，工具结果与调用仍按 id 对应。
 *
 * <p>生效范围：默认的 ToolCallingManager Bean 带 @ConditionalOnMissingBean，本 Bean 会顶掉它；
 * OpenAiChatAutoConfiguration 直接注入本 Bean 构建 OpenAiChatModel，而 OpenCodeGoChatProvider 基于该模型
 * 拷贝构造（Builder 拷贝构造会继承 toolCallingManager），因此 DeepSeek 与 OpenCode Go 两条链路同时生效。
 * 对 DeepSeek 无副作用（该字段本就是可选项）。
 */
@Slf4j
@Configuration
public class ToolCallingManagerConfig {

    @Bean
    public ToolCallingManager toolCallingManager(ToolCallbackResolver toolCallbackResolver,
                                                 ToolExecutionExceptionProcessor toolExecutionExceptionProcessor,
                                                 ObjectProvider<ObservationRegistry> observationRegistryProvider) {
        ToolCallingManager delegate = ToolCallingManager.builder()
                .observationRegistry(observationRegistryProvider.getIfUnique(() -> ObservationRegistry.NOOP))
                .toolCallbackResolver(toolCallbackResolver)
                .toolExecutionExceptionProcessor(toolExecutionExceptionProcessor)
                .build();
        log.info("工具调用管理器已装配：tool 结果消息不再回传 name 字段（规避 OpenCode Go 网关 400）");
        return new ToolResponseNameStripper(delegate);
    }

    /** 委托默认管理器执行工具，只在回传历史里抹掉 tool 消息的 name 字段 */
    private record ToolResponseNameStripper(ToolCallingManager delegate) implements ToolCallingManager {

        @Override
        public List<ToolDefinition> resolveToolDefinitions(ToolCallingChatOptions toolCallingChatOptions) {
            return delegate.resolveToolDefinitions(toolCallingChatOptions);
        }

        @Override
        public ToolExecutionResult executeToolCalls(Prompt prompt, ChatResponse chatResponse) {
            ToolExecutionResult result = delegate.executeToolCalls(prompt, chatResponse);
            List<Message> history = result.conversationHistory().stream()
                    .map(ToolResponseNameStripper::stripName)
                    .toList();
            return ToolExecutionResult.builder()
                    .conversationHistory(history)
                    .returnDirect(result.returnDirect())
                    .build();
        }

        private static Message stripName(Message message) {
            if (!(message instanceof ToolResponseMessage toolResponseMessage)) {
                return message;
            }
            List<ToolResponseMessage.ToolResponse> responses = toolResponseMessage.getResponses().stream()
                    .map(response -> new ToolResponseMessage.ToolResponse(response.id(), null, response.responseData()))
                    .toList();
            return ToolResponseMessage.builder()
                    .responses(responses)
                    .metadata(toolResponseMessage.getMetadata())
                    .build();
        }
    }
}
