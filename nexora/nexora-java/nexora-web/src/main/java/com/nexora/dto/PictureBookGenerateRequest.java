package com.nexora.dto;

/**
 * 绘本生成入参
 */
public class PictureBookGenerateRequest {

    /**
     * 绘本主题
     */
    private String topic;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}