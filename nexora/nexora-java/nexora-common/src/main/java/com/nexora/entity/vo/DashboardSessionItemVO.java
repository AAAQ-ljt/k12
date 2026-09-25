package com.nexora.entity.vo;

/**
 * 控制面板：最近 AI 会话项
 */
public class DashboardSessionItemVO {

    /** 会话ID */
    private String sessionId;

    /** 会话标题 */
    private String title;

    /** 消息数 */
    private Integer messageCount;

    /** 最后消息时间（MM-dd HH:mm） */
    private String lastMessageTime;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(Integer messageCount) {
        this.messageCount = messageCount;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(String lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }
}