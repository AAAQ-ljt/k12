package com.nexora.admin.dto;

import com.nexora.entity.po.QuestionInfo;
import com.nexora.entity.po.QuestionOption;

import java.util.List;

/**
 * 题目保存 DTO：题目主体 + 选择题选项
 */
public class QuestionSaveDTO {

    private QuestionInfo question;

    private List<QuestionOption> options;

    public QuestionInfo getQuestion() {
        return question;
    }

    public void setQuestion(QuestionInfo question) {
        this.question = question;
    }

    public List<QuestionOption> getOptions() {
        return options;
    }

    public void setOptions(List<QuestionOption> options) {
        this.options = options;
    }
}
