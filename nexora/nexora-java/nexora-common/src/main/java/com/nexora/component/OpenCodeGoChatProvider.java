package com.nexora.component;

import com.nexora.exception.BusinessException;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.UUID;

/**
 * OpenCode Go 网关供应商：走 OpenAI 兼容的 /chat/completions，默认模型 glm-5.3-flash。
 *
 * 三条 2026-09-29 带 Key 实测得到、官方文档没写或写错的事实：
 * 1. 网关**强制要求** x-opencode-session 请求头，缺失直接 400 MissingSessionID
 *    （官方页面把它描述成"优化项"，实测是硬性要求），因此这里在 OpenAiApi 层统一带上；
 * 2. 该头只认 Authorization: Bearer（Anthropic 协议的 /messages 才用 x-api-key），与 Spring AI 默认一致；
 * 3. glm-5.3-flash 支持图片输入（实测纯色图识别正确），所以文本与视觉共用同一模型。
 *
 * 第 4 条、也是延迟上最关键的一条：glm-5.3-flash 是**强制思考模型，必须显式传 reasoning_effort**。
 * 传 none / minimal 会被网关拒绝（HTTP 400：This model always engages in thinking and cannot be disabled），
 * 而不传则走网关默认档位（相当于 max）——实测 3/3 次都是先产出 1968~2349 字 reasoning_content、
 * 12.7~15.9 秒内正文一个字都没出来（输出 token 被思考耗尽，finish_reason=length）。
 * 固定 low 后 5/5 次总耗时 2.0~4.6 秒、首个正文 1.0~1.6 秒、正文完整；带图问答 4/4 次仍识别正确。
 * 由于 AgentChatComponent 只把 content 推给前端（reasoning_content 不推送），
 * 默认档位下学生端会看到一个十几秒的空气泡，这正是"回答很慢"的直接原因。
 *
 * 两个刻意的实现取舍：
 * - **客户端不注册为 Spring Bean**：Spring AI 的 OpenAiChatModel 自动装配带 @ConditionalOnMissingBean，
 *   若把本供应商的模型暴露成 Bean 会顶掉自动装配的 DeepSeek 客户端，影响默认链路；
 * - **基于自动装配模型拷贝构造**：用 OpenAiChatModel.Builder(现有模型) 继承其工具调用管理器 / 重试模板 /
 *   观测注册表，只替换 OpenAiApi，保证 MCP 知识页工具在 GLM 下照常可用。
 */
@Slf4j
@Component
public class OpenCodeGoChatProvider implements ChatProvider {

    /** 未显式配置会话 ID 时的进程级稳定值（网关只要求存在且稳定，不要求每会话唯一） */
    private static final String DEFAULT_SESSION_ID =
            "nexora-web-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);

    /**
     * 专属 User-Agent（2026-10-04）：网关前置 Cloudflare 会对"通用 HTTP 库 UA"做客户端指纹风控——
     * 实测同一请求用 Python-urllib 默认 UA 直接 403（error code: 1010），换成专属 UA 后 200；
     * 官方文档（2026-10-03 更新）同样要求"发送自身专属 user agent（如 my-coding-agent/1.0），
     * 而不是通用的 SDK 或 HTTP 库名称"。此前未设置，实际发出的是 JDK HttpClient 默认的
     * "Java-http-client/21..."，属被风控特征，这也是线上间歇 400/403 的根因。
     */
    private static final String CLIENT_USER_AGENT = "nexora-web/1.0";

    /** 构造时用于拷贝工具调用管理器等内部组件的自动装配模型 */
    @Resource
    private OpenAiChatModel openAiChatModel;

    @Resource
    private AiUsageAdvisor aiUsageAdvisor;

    @Value("${project.ai.chat.opencode-go.base-url:https://opencode.ai/zen/go/v1}")
    private String baseUrl;

    @Value("${project.ai.chat.opencode-go.completions-path:/chat/completions}")
    private String completionsPath;

    @Value("${project.ai.chat.opencode-go.api-key:}")
    private String apiKey;

    @Value("${project.ai.chat.opencode-go.model:glm-5.3-flash}")
    private String model;

    @Value("${project.ai.chat.opencode-go.session-id:}")
    private String sessionId;

    @Value("${project.ai.chat.opencode-go.reasoning-effort:low}")
    private String reasoningEffort;

    private volatile ChatClient chatClient;

    @Override
    public String code() {
        return SystemConfigComponent.PROVIDER_OPENCODE_GO;
    }

    @Override
    public String label() {
        return "OpenCode Go（" + model + "）";
    }

    @Override
    public ChatProvider current() {
        return this;
    }

    /** 懒装配：未配置 Key 时不影响应用启动，真正切到本供应商发起对话才报错 */
    @Override
    public ChatClient chatClient() {
        ChatClient client = chatClient;
        if (client == null) {
            synchronized (this) {
                client = chatClient;
                if (client == null) {
                    client = build();
                    chatClient = client;
                }
            }
        }
        return client;
    }

    @Override
    public String textModel() {
        return model;
    }

    /** GLM-5.3-Flash 本身支持图片输入（实测），视觉与文本共用同一模型 */
    @Override
    public String visionModel() {
        return model;
    }

    /**
     * 推理档位，默认 low（见类注释第 4 条：留空会走网关默认档位，导致每个问题先写约 2000 字思考、
     * 十几秒不出正文，甚至把输出 token 耗尽导致正文为空）。
     *
     * 带图同样要传：GLM-5.3-Flash 文本与视觉是同一个模型，省略参数一样会退回默认档位。
     */
    @Override
    public String reasoningEffort(boolean withImage) {
        return reasoningEffort;
    }

    /**
     * 禁用并行工具调用：Spring AI 1.1.2 的流式聚合只支持单 tool_call，
     * 而 glm-5.3-flash 经网关默认会并行返回多个工具调用（实测同一问句 2 个），
     * 触发 IllegalStateException("Currently only one tool call is supported per message") 导致整轮失败。
     * 显式传 false 后实测模型改为逐个调用，功能不变、仅多一轮往返。
     */
    @Override
    public Boolean parallelToolCalls() {
        return Boolean.FALSE;
    }

    /** 连接 10s / 读取 60s：网关不回包时走异常链路而不是无限等待（见 build() 内注释） */
    private JdkClientHttpRequestFactory openCodeGoRequestFactory() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(60));
        return factory;
    }

    private ChatClient build() {
        if (StringTools.isEmpty(apiKey)) {
            throw new BusinessException("OpenCode Go 未配置 API Key：请设置环境变量 NEXORA_OPENCODE_GO_API_KEY 后重启服务");
        }
        String session = StringTools.isEmpty(sessionId) ? DEFAULT_SESSION_ID : sessionId.trim();

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("x-opencode-session", session);
        // 专属 UA：规避 Cloudflare 对通用 HTTP 库客户端的指纹风控（见 CLIENT_USER_AGENT 注释）
        headers.add("User-Agent", CLIENT_USER_AGENT);

        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .completionsPath(completionsPath)
                .headers(headers)
                // 超时兜底（2026-10-08）：网关偶尔不回包（实测同 Key 同 session 3.9s，新 session 首包 44.8s，
                // 也有整轮挂住 9 分钟不回的情况），默认 JDK HttpClient 没有读超时 → 学生会一直看到「正在思考」。
                // 这里给连接 10s / 读取 60s 上限，超时走正常异常链路（前端能收到失败提示），不再无限等待。
                .restClientBuilder(RestClient.builder().requestFactory(openCodeGoRequestFactory()))
                .build();

        OpenAiChatModel goChatModel = new OpenAiChatModel.Builder(openAiChatModel)
                .openAiApi(openAiApi)
                .defaultOptions(OpenAiChatOptions.builder().model(model).build())
                .build();

        log.info("OpenCode Go 对话供应商已装配: baseUrl={} completionsPath={} model={} sessionId={}",
                baseUrl, completionsPath, model, session);

        return ChatClient.builder(goChatModel).defaultAdvisors(aiUsageAdvisor).build();
    }
}
