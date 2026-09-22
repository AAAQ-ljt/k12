package com.nexora.vo;

/**
 * 学习路径摘要（路线库卡片：不含节点明细）
 */
public class LearningPathSummaryVO {

    private String pathId;

    /** 路线标题 */
    private String title;

    /** 总目标一句话（叙事层） */
    private String goal;

    private String stage;

    /** 0进行中 1已完成 2已放弃 */
    private Integer status;

    private Integer totalItems;

    private Integer finishedItems;

    private Integer progress;

    /** 当前节点名 */
    private String currentNodeName;

    /** 阶段数量 */
    private Integer stageCount;

    /** 是否旧版生成（无叙事层，仅节点） */
    private Boolean legacy;

    private String createTime;

    private String updateTime;

    public String getPathId() {
        return pathId;
    }

    public void setPathId(String pathId) {
        this.pathId = pathId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }

    public Integer getFinishedItems() {
        return finishedItems;
    }

    public void setFinishedItems(Integer finishedItems) {
        this.finishedItems = finishedItems;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public String getCurrentNodeName() {
        return currentNodeName;
    }

    public void setCurrentNodeName(String currentNodeName) {
        this.currentNodeName = currentNodeName;
    }

    public Integer getStageCount() {
        return stageCount;
    }

    public void setStageCount(Integer stageCount) {
        this.stageCount = stageCount;
    }

    public Boolean getLegacy() {
        return legacy;
    }

    public void setLegacy(Boolean legacy) {
        this.legacy = legacy;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}
