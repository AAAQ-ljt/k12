package com.nexora.admin.vo;

import com.nexora.entity.po.CourseLessonQuiz;
import com.nexora.entity.po.QuestionInfo;

import java.util.List;
import java.util.Map;

/**
 * 课时通关测验详情：配置 + 已关联题目 + 每题分值
 */
public class LessonQuizDetailVO {

    /** 测验配置（quizMode=0 时为空） */
    private CourseLessonQuiz quiz;

    /** 已关联题目 */
    private List<QuestionInfo> questions;

    /** 每题分值（questionId → 分）；缺省用题目自带分值 */
    private Map<String, Integer> questionScores;

    /** 多选题漏选（未错选）按比例部分给分 */
    private Boolean partialCredit;

    public CourseLessonQuiz getQuiz() {
        return quiz;
    }

    public void setQuiz(CourseLessonQuiz quiz) {
        this.quiz = quiz;
    }

    public List<QuestionInfo> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionInfo> questions) {
        this.questions = questions;
    }

    public Map<String, Integer> getQuestionScores() {
        return questionScores;
    }

    public void setQuestionScores(Map<String, Integer> questionScores) {
        this.questionScores = questionScores;
    }

    public Boolean getPartialCredit() {
        return partialCredit;
    }

    public void setPartialCredit(Boolean partialCredit) {
        this.partialCredit = partialCredit;
    }
}
