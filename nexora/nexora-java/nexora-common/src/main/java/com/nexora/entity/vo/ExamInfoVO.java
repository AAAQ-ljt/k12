package com.nexora.entity.vo;

import com.nexora.entity.po.ExamInfo;

/**
 * 考试列表 VO：附加试卷名称
 */
public class ExamInfoVO extends ExamInfo {

    private String paperName;

    public String getPaperName() {
        return paperName;
    }

    public void setPaperName(String paperName) {
        this.paperName = paperName;
    }
}
