package com.nexora.dto;

/**
 * AI 回复取消请求
 */
public class AgentCancelRequest {

    private String messageId;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
