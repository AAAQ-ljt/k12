package com.nexora.admin.dto;

/**
 * 从资源导入知识文档请求。
 */
public class ResourceKnowledgeImportRequest {

    /**
     * 资源ID
     */
    private String resourceId;

    /**
     * 知识文档标题，为空时默认使用资源名
     */
    private String title;

    /**
     * 学段
     */
    private String stage;

    /**
     * 知识点ID
     */
    private String knowledgePointId;

    /**
     * 难度：1-3
     */
    private Integer difficulty;

    /**
     * 来源：1资料解析 2手动填写资源说明
     */
    private Integer sourceType;

    /**
     * 手动填写内容；为空时按资料解析处理
     */
    private String content;

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getKnowledgePointId() {
        return knowledgePointId;
    }

    public void setKnowledgePointId(String knowledgePointId) {
        this.knowledgePointId = knowledgePointId;
    }

    public Integer getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getSourceType() {
        return sourceType;
    }

    public void setSourceType(Integer sourceType) {
        this.sourceType = sourceType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
