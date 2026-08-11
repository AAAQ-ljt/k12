package com.nexora.mcp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Nexora MCP 教学工具服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.nexora"})
@MapperScan(basePackages = {"com.nexora.mappers"})
public class NexoraMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexoraMcpServerApplication.class, args);
    }
}
