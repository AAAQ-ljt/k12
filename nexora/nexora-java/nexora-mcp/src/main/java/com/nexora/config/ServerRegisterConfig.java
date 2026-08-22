package com.nexora.config;

import com.nexora.service.TeachingToolService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MCP 工具注册：TeachingToolService 的 @Tool 方法注册为 MCP 工具（Streamable HTTP）
 */
@Configuration
public class ServerRegisterConfig {

    @Bean
    public ToolCallbackProvider teachingToolProvider(TeachingToolService teachingToolService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(teachingToolService)
                .build();
    }
}