package com.smart.campus.web.entity.vo.analysis;

import java.io.Serializable;

public class LearningAnalysisAdviceItemVO implements Serializable {

    private String index;
    private String title;
    private String desc;

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
