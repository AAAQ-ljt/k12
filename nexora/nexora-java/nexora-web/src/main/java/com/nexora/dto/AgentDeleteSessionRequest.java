package com.nexora.dto;

/**
 * AI 会话删除请求
 */
public class AgentDeleteSessionRequest {

    private String sessionId;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
