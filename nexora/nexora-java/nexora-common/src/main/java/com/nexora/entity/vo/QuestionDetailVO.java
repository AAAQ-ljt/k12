package com.nexora.entity.vo;

import com.nexora.entity.po.QuestionInfo;
import com.nexora.entity.po.QuestionOption;

import java.util.List;

/**
 * 题目详情视图：题目 + 选项
 */
public class QuestionDetailVO {

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
