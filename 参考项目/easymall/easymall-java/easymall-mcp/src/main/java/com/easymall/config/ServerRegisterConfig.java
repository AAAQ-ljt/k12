package com.easymall.config;

import com.easymall.service.OrderService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServerRegisterConfig {

    @Bean
    public ToolCallbackProvider orderTools(OrderService orderService) {
        return MethodToolCallbackProvider.builder().toolObjects(orderService).build();
    }
}
