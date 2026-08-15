package com.nexora.entity.query;

/**
 * 试卷查询参数
 */
public class PaperInfoQuery extends BaseParam {

    private String paperNameFuzzy;

    private String grade;

    private Integer paperType;

    private Integer status;

    public String getPaperNameFuzzy() {
        return paperNameFuzzy;
    }

    public void setPaperNameFuzzy(String paperNameFuzzy) {
        this.paperNameFuzzy = paperNameFuzzy;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public Integer getPaperType() {
        return paperType;
    }

    public void setPaperType(Integer paperType) {
        this.paperType = paperType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
