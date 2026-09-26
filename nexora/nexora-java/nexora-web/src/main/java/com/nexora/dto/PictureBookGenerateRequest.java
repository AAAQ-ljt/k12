package com.nexora.dto;

/**
 * 绘本生成入参
 */
public class PictureBookGenerateRequest {

    /**
     * 绘本主题
     */
    private String topic;

    /**
     * 旁白音色（用户自选，须命中 TtsProvider 白名单）；为空回落学段默认
     */
    private String voice;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getVoice() {
        return voice;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }
}