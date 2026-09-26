package com.nexora.admin.dto;

/**
 * 语音合成测试入参（管理端模型验证）
 */
public class TtsTestRequest {

    /** 待合成文本 */
    private String text;

    /** 音色（须命中 TtsProvider 白名单；空=全局默认） */
    private String voice;

    /** 语气/风格指令（可选） */
    private String tone;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getVoice() {
        return voice;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }

    public String getTone() {
        return tone;
    }

    public void setTone(String tone) {
        this.tone = tone;
    }
}
