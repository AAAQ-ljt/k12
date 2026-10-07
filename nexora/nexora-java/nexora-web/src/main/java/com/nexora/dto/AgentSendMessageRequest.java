package com.nexora.dto;

import java.util.List;

/**
 * AI 对话发送请求
 */
public class AgentSendMessageRequest {

    /**
     * 会话ID，为空时后端自动创建新会话
     */
    private String sessionId;

    /**
     * 用户消息
     */
    private String message;

    /**
     * 随消息携带的图片资源ID（学生个人资源中心的 IMAGE 资源），可空；带图时对话走视觉模型
     */
    private List<String> imageResourceIds;

    /** 新会话场景：0 自由对话 / 3 编程练习（仅新建会话时生效） */
    private Integer scene;

    /** 新会话标题（仅新建会话时生效，如「编程练习 · 星星塔」） */
    private String sessionTitle;

    /** 重命名会话用的新名称 */
    private String title;

    /** 置顶：0否 1是 */
    private Integer top;

    /**
     * 学生当前学习上下文（计划 C/7.60 收敛项）：由学习路径节点等入口带来，
     * 服务端只把它拼进系统提示词，**不写进消息正文**——聊天气泡里不再出现那串机器味儿的文字。
     */
    private String learningContext;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getImageResourceIds() {
        return imageResourceIds;
    }

    public void setImageResourceIds(List<String> imageResourceIds) {
        this.imageResourceIds = imageResourceIds;
    }

    public Integer getScene() {
        return scene;
    }

    public void setScene(Integer scene) {
        this.scene = scene;
    }

    public String getSessionTitle() {
        return sessionTitle;
    }

    public void setSessionTitle(String sessionTitle) {
        this.sessionTitle = sessionTitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getTop() {
        return top;
    }

    public void setTop(Integer top) {
        this.top = top;
    }

    public String getLearningContext() {
        return learningContext;
    }

    public void setLearningContext(String learningContext) {
        this.learningContext = learningContext;
    }
}