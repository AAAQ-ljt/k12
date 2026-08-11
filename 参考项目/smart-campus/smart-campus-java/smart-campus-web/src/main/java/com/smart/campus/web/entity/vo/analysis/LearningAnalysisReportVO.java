package com.smart.campus.web.entity.vo.analysis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LearningAnalysisReportVO implements Serializable {

    private Integer score;
    private String summary;
    private List<String> tags = new ArrayList<>();
    private List<LearningAnalysisAdviceItemVO> adviceList = new ArrayList<>();

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<LearningAnalysisAdviceItemVO> getAdviceList() {
        return adviceList;
    }

    public void setAdviceList(List<LearningAnalysisAdviceItemVO> adviceList) {
        this.adviceList = adviceList;
    }
}
