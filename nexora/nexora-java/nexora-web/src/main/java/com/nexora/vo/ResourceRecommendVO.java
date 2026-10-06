package com.nexora.vo;

/**
 * AI 助教资料推荐卡片
 */
public class ResourceRecommendVO {

    private String docId;

    private String title;

    private String resourceId;

    private String resourceType;

    private String sourceUrl;

    /**
     * 资源归属：空 = 公共（课程/官方）资源，非空 = 该学生的个人资源。
     * 前端据此决定跳转目标——个人资源在「知识中心」预览，课程资源才去课程教材页
     */
    private String ownerId;

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
}
