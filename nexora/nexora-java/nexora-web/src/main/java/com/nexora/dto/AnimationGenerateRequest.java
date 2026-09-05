package com.nexora.dto;

/**
 * 动画讲解生成请求
 */
public class AnimationGenerateRequest {

    /** 动画主题/知识点 */
    private String topic;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}