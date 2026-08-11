package com.easymall.websocket.message;


import com.easymall.entity.dto.MessageSendDTO;
import org.springframework.stereotype.Component;

@Component("messageHandler")
public interface MessageHandler {

    void listenMessage();

    void sendMessage(MessageSendDTO sendDto);
}
