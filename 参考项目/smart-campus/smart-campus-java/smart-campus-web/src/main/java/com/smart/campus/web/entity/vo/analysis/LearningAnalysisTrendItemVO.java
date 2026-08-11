package com.smart.campus.web.entity.vo.analysis;

import java.io.Serializable;
import java.math.BigDecimal;

public class LearningAnalysisTrendItemVO implements Serializable {

    private String date;
    private String label;
    private BigDecimal value;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}
