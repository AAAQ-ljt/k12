package com.nexora.entity.vo;

/**
 * 学习分析-AI对话意图汇总
 */
public class AiIntentVO {

    private String intent;
    private Long messageCount;
    private Long tokenCount;

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public Long getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(Long messageCount) {
        this.messageCount = messageCount;
    }

    public Long getTokenCount() {
        return tokenCount;
    }

    public void setTokenCount(Long tokenCount) {
        this.tokenCount = tokenCount;
    }
}
