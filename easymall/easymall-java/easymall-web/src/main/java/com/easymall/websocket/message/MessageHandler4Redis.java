package com.easymall.websocket.message;

import com.easymall.entity.dto.MessageSendDTO;
import com.easymall.component.RedisComponent;
import com.easymall.utils.JsonUtils;
import com.easymall.websocket.ChannelContextUtils;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MessageHandler4Redis implements MessageHandler {

    private static final String MESSAGE_TOPIC = "message.topic";

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private ChannelContextUtils channelContextUtils;

    @Resource
    private RedisComponent redisComponent;

    @Override
    public void listenMessage() {
        RTopic rTopic = redissonClient.getTopic(MESSAGE_TOPIC);
        rTopic.addListener(MessageSendDTO.class, (MessageSendDTO, sendDto) -> {
            log.info("redis收到消息:{}", JsonUtils.convertObj2Json(sendDto));
            channelContextUtils.sendMessage(sendDto);
        });
    }

    @Override
    public void sendMessage(MessageSendDTO sendDto) {
        //判断消息是否取消，如果取消不发送消息
        if (redisComponent.hasCancelMessage(sendDto.getUserId(), sendDto.getMessageId())) {
            return;
        }
        RTopic rTopic = redissonClient.getTopic(MESSAGE_TOPIC);
        rTopic.publish(sendDto);
    }

    @PreDestroy
    public void destroy() {
        redissonClient.shutdown();
    }
}
