package com.nexora.dto;

/**
 * 节点快测单题作答
 *
 * @param index      题号（与题目列表 index 对应，0 起）
 * @param userAnswer 学生所选选项文本（如 "A. 内容"），判分与服务端保留的正确答案做精确比对
 */
public class NodeQuizAnswerDTO {

    /** 题号（0 起，与题目列表 index 对应） */
    private int index;

    /** 学生所选选项文本（与 options 中的选项一致） */
    private String userAnswer;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }
}