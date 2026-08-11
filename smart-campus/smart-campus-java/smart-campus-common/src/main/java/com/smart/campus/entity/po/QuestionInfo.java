package com.smart.campus.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.smart.campus.entity.enums.DateTimePatternEnum;
import com.smart.campus.utils.DateUtil;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;


/**
 * 习题题目表
 */
public class QuestionInfo implements Serializable {


    /**
     * 主键ID
     */
    private Integer questionId;

    /**
     * 题目类型:1单选 2多选 3判断 4填空
     */
    private Integer questionType;

    /**
     * 题目标题
     */
    private String questionTitle;

    /**
     * 题目配图，可为空，关联resource_info.resource_id多个用逗号隔开
     */
    private String questionImage;

    /**
     * 难度等级:1简单 2较易 3中等 4较难 5困难
     */
    private Integer difficultyLevel;

    /**
     * 标准答案，建议存JSON或统一文本，如果是选择题，存储选择题选项ID,exercise_question_option.option_id
     */
    private String correctAnswer;

    /**
     * 答案解析
     */
    private String answerAnalysis;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;


    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public Integer getQuestionId() {
        return this.questionId;
    }

    public void setQuestionType(Integer questionType) {
        this.questionType = questionType;
    }

    public Integer getQuestionType() {
        return this.questionType;
    }

    public void setQuestionTitle(String questionTitle) {
        this.questionTitle = questionTitle;
    }

    public String getQuestionTitle() {
        return this.questionTitle;
    }

    public void setQuestionImage(String questionImage) {
        this.questionImage = questionImage;
    }

    public String getQuestionImage() {
        return this.questionImage;
    }

    public void setDifficultyLevel(Integer difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public Integer getDifficultyLevel() {
        return this.difficultyLevel;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getCorrectAnswer() {
        return this.correctAnswer;
    }

    public void setAnswerAnalysis(String answerAnalysis) {
        this.answerAnalysis = answerAnalysis;
    }

    public String getAnswerAnalysis() {
        return this.answerAnalysis;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getUpdateTime() {
        return this.updateTime;
    }

    @Override
    public String toString() {
        return "主键ID:" + (questionId == null ? "空" : questionId) + "，题目类型:1单选 2多选 3判断 4填空 5简答:" + (questionType == null ? "空" : questionType) + "，题目标题:" + (questionTitle == null ? "空" : questionTitle) + "，题目配图，可为空，关联resource_info.resource_id多个用逗号隔开:" + (questionImage == null ? "空" : questionImage) + "，难度等级:1简单 2较易 3中等 4较难 5困难:" + (difficultyLevel == null ? "空" : difficultyLevel) + "，标准答案，建议存JSON或统一文本，如果是选择题，存储选择题选项ID,exercise_question_option.option_id:" + (correctAnswer == null ? "空" : correctAnswer) + "，答案解析:" + (answerAnalysis == null ? "空" : answerAnalysis) + "，创建时间:" + (createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + "，更新时间:" + (updateTime == null ? "空" : DateUtil.format(updateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
    }
}
