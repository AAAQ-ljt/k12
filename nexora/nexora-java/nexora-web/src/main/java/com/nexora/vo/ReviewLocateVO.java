package com.nexora.vo;

/**
 * 待复习知识点定位结果：命中该知识点所在的学习路径节点时跳到路线做节点快测，
 * 未命中（located=false）则前端转 AI 助教对话复习。
 */
public class ReviewLocateVO {

    /** 是否在用户的学习路径中找到对应节点 */
    private boolean located;

    /** 命中的节点所属路径ID */
    private String pathId;

    /** 命中的节点ID */
    private String itemId;

    /** 命中的知识点/节点名 */
    private String knowledgePointName;

    public boolean isLocated() {
        return located;
    }

    public void setLocated(boolean located) {
        this.located = located;
    }

    public String getPathId() {
        return pathId;
    }

    public void setPathId(String pathId) {
        this.pathId = pathId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getKnowledgePointName() {
        return knowledgePointName;
    }

    public void setKnowledgePointName(String knowledgePointName) {
        this.knowledgePointName = knowledgePointName;
    }
}