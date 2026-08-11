package com.easymall.websocket;

import com.alibaba.fastjson2.JSON;
import com.easymall.entity.dto.MessageSendDTO;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component("channelContextUtils")
@Slf4j
public class ChannelContextUtils {

    public static final ConcurrentMap<String, Channel> USER_CONTEXT_MAP = new ConcurrentHashMap();

    public void sendMessage(MessageSendDTO messageSendDto) {
        Channel channel = USER_CONTEXT_MAP.get(messageSendDto.getUserId());
        if (channel == null || !channel.isActive()) {
            return;
        }
        channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(messageSendDto)));
    }

    public void addContext(String userId, Channel channel) {
        try {
            String channelId = channel.id().toString();
            AttributeKey attributeKey = null;
            if (!AttributeKey.exists(channelId)) {
                attributeKey = AttributeKey.newInstance(channel.id().toString());
            } else {
                attributeKey = AttributeKey.valueOf(channel.id().toString());
            }
            channel.attr(attributeKey).set(userId);
            USER_CONTEXT_MAP.put(userId, channel);
        } catch (Exception e) {
            log.error("初始化链接失败", e);
        }
    }
}