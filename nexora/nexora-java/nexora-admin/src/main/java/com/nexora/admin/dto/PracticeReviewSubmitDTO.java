package com.nexora.admin.dto;

/**
 * 答题批阅提交请求
 */
public class PracticeReviewSubmitDTO {

    /** 作答流水ID */
    private Long recordId;

    /** 批阅得分（0 ~ 题目满分） */
    private Integer reviewScore;

    /** 批阅评语（可选） */
    private String reviewComment;

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Integer getReviewScore() {
        return reviewScore;
    }

    public void setReviewScore(Integer reviewScore) {
        this.reviewScore = reviewScore;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }
}
