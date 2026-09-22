package com.nexora.vo;

import java.util.List;

/**
 * 个性化学习路径（学生端页面）：路径 + 主线节点 + 兴趣分支
 */
public class LearningPathVO {

    /** 路径ID */
    private String pathId;

    /** 路径标题 */
    private String title;

    /** 学段 */
    private String stage;

    /** 状态：0进行中 1已完成 2已放弃 */
    private Integer status;

    /** 节点总数 */
    private Integer totalItems;

    /** 已完成节点数 */
    private Integer finishedItems;

    /** 进度百分比 */
    private Integer progress;

    /** 当前节点名（AI 主动引导锚点） */
    private String currentNodeId;

    private String currentNodeName;

    private String createTime;

    private String updateTime;

    /** 总目标（叙事层） */
    private String goal;

    /** 产出物（叙事层） */
    private String outcome;

    /** 建议节奏（叙事层） */
    private String cadence;

    /** 起点建议（叙事层） */
    private String startHint;

    /** 主线阶段（含阶段目标/验收/节点） */
    private List<LearningPathStageVO> stages;

    /** 兴趣分支（选学，按分支名分组） */
    private List<LearningPathBranchVO> branches;

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

    public String getCurrentNodeId() {
        return currentNodeId;
    }

    public void setCurrentNodeId(String currentNodeId) {
        this.currentNodeId = currentNodeId;
    }

    public String getCurrentNodeName() {
        return currentNodeName;
    }

    public void setCurrentNodeName(String currentNodeName) {
        this.currentNodeName = currentNodeName;
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

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public String getCadence() {
        return cadence;
    }

    public void setCadence(String cadence) {
        this.cadence = cadence;
    }

    public String getStartHint() {
        return startHint;
    }

    public void setStartHint(String startHint) {
        this.startHint = startHint;
    }

    public List<LearningPathStageVO> getStages() {
        return stages;
    }

    public void setStages(List<LearningPathStageVO> stages) {
        this.stages = stages;
    }

    public List<LearningPathBranchVO> getBranches() {
        return branches;
    }

    public void setBranches(List<LearningPathBranchVO> branches) {
        this.branches = branches;
    }
}
