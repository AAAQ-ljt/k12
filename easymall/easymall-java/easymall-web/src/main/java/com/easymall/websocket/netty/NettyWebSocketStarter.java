package com.easymall.websocket.netty;

import com.easymall.entity.config.AppConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @Description: ws初始化类
 * @auther: 程序员老罗 https://space.bilibili.com/499388891
 * @date: 9:36 2025/6/22
 * @param:
 * @return:
 */
@Component
@Slf4j
public class NettyWebSocketStarter implements Runnable {

    @Resource
    private AppConfig appConfig;

    @Resource
    private HandlerWebSocket handlerWebSocket;

    @Resource
    private HandlerTokenValidation handlerTokenValidation;
    /**
     * boss线程组，用于处理连接
     */
    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    /**
     * work线程组，用于处理消息
     */
    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    /**
     * 资源关闭——在容器销毁时关闭
     */
    @PreDestroy
    public void close() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    @Override
    public void run() {
        try {
            //创建服务端启动助手
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup);
            serverBootstrap.channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel channel) {
                            ChannelPipeline pipeline = channel.pipeline();
                            /**
                             * 对http协议的支持，使用http的编码器，解码器
                             * 通常作为第一个处理器添加
                             * 必须在 HttpObjectAggregator 之前
                             */
                            pipeline.addLast(new HttpServerCodec());
                            /**
                             * 这是一个 HTTP 消息聚合器，主要功能是将分片的 HTTP 消息
                             * （如 chunked 传输编码的消息）聚合成完整的 FullHttpRequest 或 FullHttpResponse。
                             */
                            pipeline.addLast(new HttpObjectAggregator(64 * 1024));

                            /**
                             * 检测连接空闲状态的处理器 会传递给下一个处理器
                             * readerIdleTime  一段时间内未收到客户端数据。
                             * writerIdleTime  一段时间内未向客户端发送数据。
                             * allIdleTime  -读和写均无活动
                             */
                            pipeline.addLast(new IdleStateHandler(60, 0, 0, TimeUnit.SECONDS));
                            /**
                             * 处理空闲事件
                             */
                            pipeline.addLast(new HandlerHeartBeat());
                            /**
                             * 拦截 channelRead 事件
                             * TOKEN校验
                             */
                            pipeline.addLast(handlerTokenValidation);
                            /**
                             * WebSocket协议处理器（关键配置）
                             * websocketPath 指定 WebSocket 的端点路径
                             * subprotocols 指定支持的子协议
                             * allowExtensions  是否允许 WebSocket 扩展
                             * maxFrameSize  设置最大帧大小 65536
                             * allowMaskMismatch 是否允许掩码不匹配
                             * checkStartsWith 是否严格检查路径开头
                             * handshakeTimeoutMillis  握手超时时间（毫秒）
                             */
                            pipeline.addLast(new WebSocketServerProtocolHandler("/ws", null, true, 65536, true, true, 10000L));
                            pipeline.addLast(handlerWebSocket);
                        }
                    });
            //启动
            Channel channel = serverBootstrap.bind(appConfig.getWsPort()).sync().channel();
            log.info("Netty服务端启动成功,端口:{}", appConfig.getWsPort());
            channel.closeFuture().sync();
        } catch (Exception e) {
            log.error("netty启动失败", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}