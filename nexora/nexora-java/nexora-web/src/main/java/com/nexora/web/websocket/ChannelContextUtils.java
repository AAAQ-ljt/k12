package com.nexora.web.websocket;

import io.netty.channel.Channel;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 用户通道上下文管理
 */
@Component
public class ChannelContextUtils {

    private final ConcurrentHashMap<String, Channel> userChannelMap = new ConcurrentHashMap<>();

    /**
     * 添加用户与 Channel 的绑定关系
     */
    public void addContext(String userId, Channel channel) {
        userChannelMap.put(userId, channel);
    }

    /**
     * 获取用户的 Channel
     */
    public Channel getChannel(String userId) {
        return userChannelMap.get(userId);
    }

    /**
     * 移除用户上下文
     */
    public void removeContext(String userId) {
        userChannelMap.remove(userId);
    }

    /**
     * 关闭用户 Channel 并移除上下文
     */
    public void closeChannel(String userId) {
        Channel channel = userChannelMap.remove(userId);
        if (channel != null) {
            channel.close();
        }
    }
}
