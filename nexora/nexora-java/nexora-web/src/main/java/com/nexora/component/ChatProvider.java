package com.nexora.component;

import org.springframework.ai.chat.client.ChatClient;

/**
 * 对话模型供应商统一入口，与文生图 {@link ImageProvider} 同一套设计：
 * 由配置项 project.ai.chat.provider（可被 system_config 的 AI_MODEL.chat_provider 运行时覆盖）
 * 决定当前生效的实现，业务侧只注入本接口，不直接依赖具体厂商。
 *
 * 现有实现：deepseek（默认，直连 DeepSeek 官方）/ opencode-go（OpenCode Go 网关的 glm-5.3-flash）。
 */
public interface ChatProvider {

    /** 供应商编码：deepseek / opencode-go */
    String code();

    /** 供应商展示名（日志与运行时信息展示用） */
    String label();

    /**
     * 当前生效的具体供应商实例。路由实现返回被选中的那个，具体供应商返回自身。
     * 一次对话内应先用本方法取一次快照，再用它读客户端与模型名，
     * 避免切换瞬间出现「A 的客户端配 B 的模型名」导致 400。
     */
    ChatProvider current();

    /** 已完成 Advisor 装配的对话客户端 */
    ChatClient chatClient();

    /** 当前供应商的文本模型 ID */
    String textModel();

    /** 当前供应商的视觉模型 ID；不支持图片时返回文本模型 */
    String visionModel();

    /**
     * 本次请求要传给模型的 reasoning-effort；返回空表示**不带该参数**。
     *
     * 是否传、传什么由各供应商自己决定，因为差异是供应商级的：
     * DeepSeek 的视觉模型不接受该参数（带了会 400），所以带图时必须省略；
     * 而 OpenCode Go 的 GLM-5.3-Flash 是强制思考模型，**恰恰相反**——不传就会走网关默认档位、
     * 正文要等 30 秒以上，所以带图也必须显式传 low。
     *
     * @param withImage 本次请求是否携带图片
     */
    String reasoningEffort(boolean withImage);

    /**
     * 本次请求要传给模型的 parallel_tool_calls 取值；返回 null 表示**不带该参数**。
     *
     * 供应商级差异，原因：Spring AI 1.1.2 的流式工具调用聚合（OpenAiStreamFunctionCallingHelper.merge）
     * 只支持「一条消息一个工具调用」，模型返回多个并行 tool_calls 时直接抛
     * IllegalStateException("Currently only one tool call is supported per message")。
     * OpenCode Go 的 glm-5.3-flash 经网关默认并行调用（2026-10-04 实测同一问句返回 2 个 tool_calls），
     * 必须显式传 false 让它逐个串行调用；DeepSeek 直连未触发该问题，保持不带参数（null）。
     */
    default Boolean parallelToolCalls() {
        return null;
    }
}
