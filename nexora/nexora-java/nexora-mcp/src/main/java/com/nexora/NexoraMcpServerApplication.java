package com.nexora;

import com.nexora.component.AiStructureComponent;
import com.nexora.component.ChatProviderRouter;
import com.nexora.component.DeepSeekChatProvider;
import com.nexora.component.OpenCodeGoChatProvider;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Nexora MCP 教学工具服务启动类
 *
 * <p>排除**对话端组件**（MCP 是纯工具服务，不装配任何对话模型，这些 Bean 一旦被扫描到就会因为
 * 找不到 OpenAiChatModel / ChatClient 而让应用起不来）：
 * <ul>
 *   <li>{@code AiStructureComponent}：依赖 ChatClient Bean；</li>
 *   <li>{@code ChatProviderRouter} / {@code DeepSeekChatProvider} / {@code OpenCodeGoChatProvider}：
 *       2026-10-07 从 nexora-web 迁入 nexora-common 后共用，依赖自动装配的 {@code OpenAiChatModel}
 *       —— 迁入当天 MCP 就因为被扫到而启动失败（APPLICATION FAILED TO START），故一并排除。</li>
 * </ul>
 *
 * <p>不要顺手把 AiUsageAdvisor 也加进 excludeFilters：注解里的类字面量会强制加载该类，
 * 而加载它必须能解析其实现的 Spring AI 接口，classpath 缺 spring-ai-client-chat 时反而会抛
 * NoClassDefFoundError。该依赖已显式声明在 nexora-mcp/pom.xml。
 * 同理：上面的 ChatProvider 家族引用 {@code OpenAiChatModel}（spring-ai-openai），
 * 该依赖由 nexora-common 传递进来，**不要从 common 改成 optional**，否则这里的类字面量会加载失败。
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.nexora"},
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {AiStructureComponent.class, ChatProviderRouter.class,
                        DeepSeekChatProvider.class, OpenCodeGoChatProvider.class}))
@MapperScan(basePackages = {"com.nexora.mappers"})
public class NexoraMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexoraMcpServerApplication.class, args);
    }
}