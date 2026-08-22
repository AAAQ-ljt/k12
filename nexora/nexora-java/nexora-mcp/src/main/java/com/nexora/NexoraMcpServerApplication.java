package com.nexora;

import com.nexora.component.AiStructureComponent;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Nexora MCP 教学工具服务启动类
 * 排除 AiStructureComponent：它依赖 ChatClient Bean（对话端能力），MCP 不装配对话模型
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.nexora"},
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AiStructureComponent.class))
@MapperScan(basePackages = {"com.nexora.mappers"})
public class NexoraMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexoraMcpServerApplication.class, args);
    }
}