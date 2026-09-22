package com.nexora.vo;

import java.util.List;

/**
 * 学生端「我的掌握度概览」：学习进度卡片数据源（个性化学习路径页展示真实进度）
 */
public class KnowledgeMasteryOverviewVO {

    /** 已掌握知识点数（status=2） */
    private Integer masteredCount;

    /** 进行中知识点数（status=1） */
    private Integer learningCount;

    /** 有掌握度记录的知识点总数 */
    private Integer totalCount;

    /** 平均掌握度（百分制，四舍五入） */
    private Integer avgMasteryScore;

    /** 累计练习次数 */
    private Integer totalPractice;

    /** 累计答对次数 */
    private Integer totalCorrect;

    /** 正确率（百分制，四舍五入） */
    private Integer correctRate;

    /** 待复习数量（已到复习时间的知识点） */
    private Integer dueReviewCount;

    /** 知识点掌握度明细（按最近练习时间倒序） */
    private List<KnowledgeMasteryItemVO> items;

    public Integer getMasteredCount() {
        return masteredCount;
    }

    public void setMasteredCount(Integer masteredCount) {
        this.masteredCount = masteredCount;
    }

    public Integer getLearningCount() {
        return learningCount;
    }

    public void setLearningCount(Integer learningCount) {
        this.learningCount = learningCount;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getAvgMasteryScore() {
        return avgMasteryScore;
    }

    public void setAvgMasteryScore(Integer avgMasteryScore) {
        this.avgMasteryScore = avgMasteryScore;
    }

    public Integer getTotalPractice() {
        return totalPractice;
    }

    public void setTotalPractice(Integer totalPractice) {
        this.totalPractice = totalPractice;
    }

    public Integer getTotalCorrect() {
        return totalCorrect;
    }

    public void setTotalCorrect(Integer totalCorrect) {
        this.totalCorrect = totalCorrect;
    }

    public Integer getCorrectRate() {
        return correctRate;
    }

    public void setCorrectRate(Integer correctRate) {
        this.correctRate = correctRate;
    }

    public Integer getDueReviewCount() {
        return dueReviewCount;
    }

    public void setDueReviewCount(Integer dueReviewCount) {
        this.dueReviewCount = dueReviewCount;
    }

    public List<KnowledgeMasteryItemVO> getItems() {
        return items;
    }

    public void setItems(List<KnowledgeMasteryItemVO> items) {
        this.items = items;
    }
}
