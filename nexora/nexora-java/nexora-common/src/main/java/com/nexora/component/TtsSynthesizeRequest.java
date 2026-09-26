package com.nexora.component;

/**
 * TTS 合成入参：待合成文本 + 音色 + 语气指令。
 * text 为要读的内容（对应上游 assistant 消息）；tone 为语气/风格指令（对应 user 消息），可为空。
 */
public record TtsSynthesizeRequest(String text, String voice, String tone) {

    public static TtsSynthesizeRequest of(String text, String voice, String tone) {
        return new TtsSynthesizeRequest(text, voice, tone);
    }
}
