package com.smart.campus.web.entity.vo.analysis;

import java.io.Serializable;

public class LearningAnalysisTimePreferenceItemVO implements Serializable {

    private String label;
    private Integer value;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
