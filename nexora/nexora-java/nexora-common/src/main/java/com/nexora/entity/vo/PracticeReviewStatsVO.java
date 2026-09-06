package com.nexora.entity.vo;

/**
 * 答题批阅统计：待批阅/已批阅/批阅均分/客观题正确率
 */
public class PracticeReviewStatsVO {

    /** 待批阅数（主观题） */
    private Long pendingCount;

    /** 已批阅数 */
    private Long reviewedCount;

    /** 已批阅平均分 */
    private Double reviewAvgScore;

    /** 客观题正确率（%） */
    private Double objectiveAccuracy;

    public Long getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(Long pendingCount) {
        this.pendingCount = pendingCount;
    }

    public Long getReviewedCount() {
        return reviewedCount;
    }

    public void setReviewedCount(Long reviewedCount) {
        this.reviewedCount = reviewedCount;
    }

    public Double getReviewAvgScore() {
        return reviewAvgScore;
    }

    public void setReviewAvgScore(Double reviewAvgScore) {
        this.reviewAvgScore = reviewAvgScore;
    }

    public Double getObjectiveAccuracy() {
        return objectiveAccuracy;
    }

    public void setObjectiveAccuracy(Double objectiveAccuracy) {
        this.objectiveAccuracy = objectiveAccuracy;
    }
}
