package com.nexora.vo;

/**
 * 学习路径节点（学生端节点墙）
 */
public class LearningPathNodeVO {

    /** 节点ID */
    private String itemId;

    /** 知识点ID */
    private String knowledgePointId;

    /** 知识点/主题名 */
    private String knowledgePointName;

    /** 0主线 1兴趣分支 */
    private Integer branchType;

    /** 分支名，可空 */
    private String branchName;

    /** 0学习 1复习 */
    private Integer itemType;

    /** 状态：0未解锁 1进行中 2已掌握（由掌握度驱动） */
    private Integer status;

    /** 排序 */
    private Integer sort;

    /** 复习到期日（yyyy-MM-dd，复习节点有效） */
    private String dueDate;

    /** 完成时间（yyyy-MM-dd HH:mm:ss，已掌握时有值） */
    private String finishTime;

    /** 掌握度 0-100 */
    private Integer masteryScore;

    /** 练习次数 */
    private Integer practiceCount;

    /** 下次复习时间（yyyy-MM-dd HH:mm:ss） */
    private String nextReviewTime;

    /** 是否为待复习节点（下次复习时间已到） */
    private Boolean due;

    /** 学习建议（来自知识点描述） */
    private String learningTip;

    /** 要做什么（叙事层：可验收的动手任务） */
    private String task;

    /** 怎么学（叙事层：学习方式） */
    private String way;

    /** 预计用时（分钟，叙事层） */
    private Integer minutes;

    /** 是否必学（叙事层：false=选学） */
    private Boolean must;

    /** 前置节点名（未解锁时给出，提示"请先完成前置节点"） */
    private String prerequisiteName;

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getKnowledgePointId() {
        return knowledgePointId;
    }

    public void setKnowledgePointId(String knowledgePointId) {
        this.knowledgePointId = knowledgePointId;
    }

    public String getKnowledgePointName() {
        return knowledgePointName;
    }

    public void setKnowledgePointName(String knowledgePointName) {
        this.knowledgePointName = knowledgePointName;
    }

    public Integer getBranchType() {
        return branchType;
    }

    public void setBranchType(Integer branchType) {
        this.branchType = branchType;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public Integer getItemType() {
        return itemType;
    }

    public void setItemType(Integer itemType) {
        this.itemType = itemType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(String finishTime) {
        this.finishTime = finishTime;
    }

    public Integer getMasteryScore() {
        return masteryScore;
    }

    public void setMasteryScore(Integer masteryScore) {
        this.masteryScore = masteryScore;
    }

    public Integer getPracticeCount() {
        return practiceCount;
    }

    public void setPracticeCount(Integer practiceCount) {
        this.practiceCount = practiceCount;
    }

    public String getNextReviewTime() {
        return nextReviewTime;
    }

    public void setNextReviewTime(String nextReviewTime) {
        this.nextReviewTime = nextReviewTime;
    }

    public Boolean getDue() {
        return due;
    }

    public void setDue(Boolean due) {
        this.due = due;
    }

    public String getLearningTip() {
        return learningTip;
    }

    public void setLearningTip(String learningTip) {
        this.learningTip = learningTip;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getWay() {
        return way;
    }

    public void setWay(String way) {
        this.way = way;
    }

    public Integer getMinutes() {
        return minutes;
    }

    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }

    public Boolean getMust() {
        return must;
    }

    public void setMust(Boolean must) {
        this.must = must;
    }

    public String getPrerequisiteName() {
        return prerequisiteName;
    }

    public void setPrerequisiteName(String prerequisiteName) {
        this.prerequisiteName = prerequisiteName;
    }
}
