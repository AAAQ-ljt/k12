package com.nexora.admin.vo;

/**
 * 官方资源 AI 文档整理结果
 */
public class KnowledgeAIDocVO {

    /**
     * 资源ID
     */
    private String resourceId;

    /**
     * 资源名
     */
    private String resourceName;

    /**
     * 学段
     */
    private String stage;

    /**
     * 原始提取文本（展示用，可能截断）
     */
    private String originalText;

    /**
     * AI 整理后的结构化 Markdown（可编辑）
     */
    private String organizedMd;

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public String getOrganizedMd() {
        return organizedMd;
    }

    public void setOrganizedMd(String organizedMd) {
        this.organizedMd = organizedMd;
    }
}