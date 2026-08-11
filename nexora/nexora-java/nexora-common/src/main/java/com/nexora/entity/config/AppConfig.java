package com.nexora.entity.config;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 应用配置
 */
@Component
public class AppConfig {

    @Value("${ws.port:6062}")
    private Integer wsPort;

    /**
     * 获取 WebSocket 端口
     */
    public Integer getWsPort() {
        return wsPort;
    }
}
