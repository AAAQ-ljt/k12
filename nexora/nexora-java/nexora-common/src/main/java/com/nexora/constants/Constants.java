package com.nexora.constants;

/**
 * 系统常量
 */
public class Constants {

    /**
     * WebSocket 心跳消息
     */
    public static final String PING = "ping";

    /**
     * WebSocket 心跳响应
     */
    public static final String PONG = "pong";

    /**
     * Redis key 前缀：用户心跳
     */
    public static final String REDIS_KEY_HEART_BEAT = "heart_beat:";

    /**
     * Redis key 前缀：用户 Token
     */
    public static final String REDIS_KEY_TOKEN = "token:";

    /**
     * 心跳超时时间（秒）
     */
    public static final int HEART_BEAT_TIMEOUT = 30;
}
