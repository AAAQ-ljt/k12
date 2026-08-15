package com.nexora.entity.query;

/**
 * 考试查询参数
 */
public class ExamInfoQuery extends BaseParam {

    private String examNameFuzzy;

    private String grade;

    private String paperId;

    private Integer status;

    public String getExamNameFuzzy() {
        return examNameFuzzy;
    }

    public void setExamNameFuzzy(String examNameFuzzy) {
        this.examNameFuzzy = examNameFuzzy;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getPaperId() {
        return paperId;
    }

    public void setPaperId(String paperId) {
        this.paperId = paperId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
