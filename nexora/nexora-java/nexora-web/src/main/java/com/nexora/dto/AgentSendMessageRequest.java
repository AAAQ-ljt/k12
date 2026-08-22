package com.nexora.dto;

import java.util.List;

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

    /**
     * 随消息携带的图片资源ID（学生个人资源中心的 IMAGE 资源），可空；带图时对话走视觉模型
     */
    private List<String> imageResourceIds;

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

    public List<String> getImageResourceIds() {
        return imageResourceIds;
    }

    public void setImageResourceIds(List<String> imageResourceIds) {
        this.imageResourceIds = imageResourceIds;
    }
}