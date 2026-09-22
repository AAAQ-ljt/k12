package com.nexora.config;

import com.nexora.service.KnowledgeToolService;
import com.nexora.service.TeachingToolService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MCP 工具注册（Streamable HTTP）：
 * 教学域工具（TeachingToolService）+ 学生个人知识页工具（KnowledgeToolService）
 */
@Configuration
public class ServerRegisterConfig {

    @Bean
    public ToolCallbackProvider teachingToolProvider(TeachingToolService teachingToolService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(teachingToolService)
                .build();
    }

    @Bean
    public ToolCallbackProvider knowledgeToolProvider(KnowledgeToolService knowledgeToolService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(knowledgeToolService)
                .build();
    }
}
