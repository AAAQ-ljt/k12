package com.nexora.web.config;

import com.nexora.web.websocket.netty.NettyWebSocketStarter;
import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Netty WebSocket 服务启动器
 */
@Component
public class NettyStarterRunner implements CommandLineRunner {

    @Resource
    private NettyWebSocketStarter nettyWebSocketStarter;

    @Override
    public void run(String... args) {
        new Thread(nettyWebSocketStarter).start();
    }
}
