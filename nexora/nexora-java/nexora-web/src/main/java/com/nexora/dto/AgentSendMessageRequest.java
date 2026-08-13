package com.nexora.dto;

/**
 * AI 对话发送请求
 */
public class AgentSendMessageRequest {

    /**
     * 会话ID，为空时后端自动创建新会话
     */
    private String sessionId;

    /**
     * 用户消息
     */
    private String message;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
