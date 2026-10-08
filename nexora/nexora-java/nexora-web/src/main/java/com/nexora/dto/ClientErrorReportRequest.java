package com.nexora.dto;

/**
 * 前端错误兜底页上报（2026-10-08）。
 *
 * 学生端出现「页面出了点小问题」时，把真实报错原文发到这里，
 * 服务端日志即可定位（此前只能在用户截图的"技术细节"里看到，排查全靠猜）。
 */
public class ClientErrorReportRequest {

    /** 错误原文（Error.message 或字符串化后的异常） */
    private String message;

    /** 出错时所在页面 URL */
    private String url;

    /** 浏览器 UA（确认环境用，可空） */
    private String userAgent;

    /** 学生学段（可空） */
    private String stage;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }
}
