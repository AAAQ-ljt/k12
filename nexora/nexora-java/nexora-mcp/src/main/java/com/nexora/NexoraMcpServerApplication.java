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
 *
 * <p>不要顺手把 AiUsageAdvisor 也加进 excludeFilters：注解里的类字面量会强制加载该类，
 * 而加载它必须能解析其实现的 Spring AI 接口，classpath 缺 spring-ai-client-chat 时反而会抛
 * NoClassDefFoundError。该依赖已显式声明在 nexora-mcp/pom.xml。
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