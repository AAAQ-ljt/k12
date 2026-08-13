package com.nexora;

import com.nexora.websocket.netty.NettyWebSocketStarter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InitRun implements ApplicationRunner {

    @Resource
    private NettyWebSocketStarter nettyWebSocketStarter;

    @Override
    public void run(ApplicationArguments args) {
        new Thread(nettyWebSocketStarter).start();
        log.info("Netty WebSocket 服务启动线程已提交");
    }
}
