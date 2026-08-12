package com.nexora.constants;

/**
 * 系统常量
 */
public class Constants {

    public static final String REGEX_PASSWORD = "^(?=.*\\d)(?=.*[a-zA-Z])[\\da-zA-Z~!@#$%^&*_]{8,18}$";
    public static final String ZERO_STR = "0";
    public static final Integer LENGTH_5 = 5;
    public static final Integer LENGTH_10 = 10;
    public static final Integer LENGTH_15 = 15;
    public static final Integer LENGTH_30 = 30;

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

    /**
     * 请求 attribute 中用户信息的 key
     */
    public static final String ATTR_USER_INFO = "userInfo";

    /**
     * 管理端 token 请求头名称
     */
    public static final String HEADER_ADMIN_TOKEN = "adminToken";

    /**
     * 学生端 token 请求头名称
     */
    public static final String HEADER_STUDENT_TOKEN = "studentToken";

    /**
     * 角色：管理员
     */
    public static final Integer ROLE_ADMIN = 0;

    /**
     * 角色：学生
     */
    public static final Integer ROLE_STUDENT = 1;

    /**
     * 状态：启用
     */
    public static final Integer STATUS_ENABLE = 1;

    /**
     * 状态：禁用
     */
    public static final Integer STATUS_DISABLE = 0;
}
