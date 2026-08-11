package com.smart.campus.admin.entity.vo;

import java.io.Serializable;
import java.math.BigDecimal;

public class PaperQuestionItemVO extends PaperQuestionSnapshotVO implements Serializable {

    private Integer id;

    private BigDecimal questionScore;

    private Integer sortOrder;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BigDecimal getQuestionScore() {
        return questionScore;
    }

    public void setQuestionScore(BigDecimal questionScore) {
        this.questionScore = questionScore;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
