package com.nexora.dto;

/**
 * 绘本旁白合成入参：page 为空=整本补录，page 给定=单页（重）录制；voice 为用户自选音色（白名单校验）
 */
public class PictureBookAudioRequest {

    /** 绘本资源ID */
    private String resourceId;

    /** 页码（0 开始）；为空表示整本补录 */
    private Integer page;

    /** 自选音色（须命中 TtsProvider 白名单）；为空回落学段默认 */
    private String voice;

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public String getVoice() {
        return voice;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }
}
