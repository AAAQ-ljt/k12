package com.nexora.websocket;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Netty WebSocket 通道上下文工具
 * 维护 userId -> Channel 的映射，用于 AI 流式回复实时推送
 */
@Component("channelContextUtils")
@Slf4j
public class ChannelContextUtils {

    public static final ConcurrentMap<String, Channel> USER_CONTEXT_MAP = new ConcurrentHashMap<>();

    /**
     * 建立连接后绑定 userId 与 Channel
     */
    public void addContext(String userId, Channel channel) {
        try {
            String channelId = channel.id().toString();
            AttributeKey<String> attributeKey;
            if (!AttributeKey.exists(channelId)) {
                attributeKey = AttributeKey.newInstance(channelId);
            } else {
                attributeKey = AttributeKey.valueOf(channelId);
            }
            channel.attr(attributeKey).set(userId);
            USER_CONTEXT_MAP.put(userId, channel);
            log.info("WebSocket 连接已绑定用户: {}", userId);
        } catch (Exception e) {
            log.error("初始化 WebSocket 连接失败", e);
        }
    }

    /**
     * 向指定用户推送文本消息（AI 流式输出）
     */
    public void sendMessage(String userId, String message) {
        Channel channel = USER_CONTEXT_MAP.get(userId);
        if (channel == null || !channel.isActive()) {
            log.warn("用户 {} 的 WebSocket 通道不可用，消息丢弃", userId);
            return;
        }
        channel.writeAndFlush(new TextWebSocketFrame(message));
    }

    /**
     * 连接断开时移除用户绑定
     */
    public void removeContext(String userId, Channel channel) {
        if (channel != null) {
            String channelId = channel.id().toString();
            AttributeKey<String> attributeKey;
            if (AttributeKey.exists(channelId)) {
                attributeKey = AttributeKey.valueOf(channelId);
                String boundUserId = channel.attr(attributeKey).get();
                if (userId.equals(boundUserId)) {
                    channel.attr(attributeKey).set(null);
                }
            }
        }
        Channel bound = USER_CONTEXT_MAP.get(userId);
        if (bound == channel) {
            USER_CONTEXT_MAP.remove(userId);
            log.info("WebSocket 连接已解除绑定用户: {}", userId);
        }
    }
}
