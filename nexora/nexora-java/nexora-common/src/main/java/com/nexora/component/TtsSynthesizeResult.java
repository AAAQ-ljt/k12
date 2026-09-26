package com.nexora.component;

/**
 * TTS 合成结果：成功返回音频字节（mp3），失败返回用户可读原因。
 */
public record TtsSynthesizeResult(byte[] audioData, String errorMessage) {

    public static TtsSynthesizeResult success(byte[] audioData) {
        return new TtsSynthesizeResult(audioData, null);
    }

    public static TtsSynthesizeResult failure(String errorMessage) {
        return new TtsSynthesizeResult(null, errorMessage);
    }

    public boolean success() {
        return audioData != null && audioData.length > 0;
    }
}
