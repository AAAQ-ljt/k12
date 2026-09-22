package com.nexora.dto;

import com.nexora.vo.NodeQuizVO;

import java.util.List;

/**
 * 节点快测提交判分请求
 *
 * 出题接口把「题目 + 正确答案」原样下发给前端（答题时不下发 answer/analysis 展示），
 * 提交时前端把题目（含答案）与作答一并回传，服务端依此做权威判分——只在前端已持有答案的
 * 前提下计算对错，绝不信任前端自带的 isCorrect 判定。
 */
public class NodeQuizSubmitDTO {

    /** 节点ID */
    private String itemId;

    /** 题目列表（回传：含答案与解析，判分依据） */
    private List<NodeQuizVO.NodeQuizQuestionVO> questions;

    /** 作答列表（须覆盖全部题目） */
    private List<NodeQuizAnswerDTO> answers;

    /** 答题用时（秒，可选，缺省按 0 记录） */
    private Integer duration;

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public List<NodeQuizVO.NodeQuizQuestionVO> getQuestions() {
        return questions;
    }

    public void setQuestions(List<NodeQuizVO.NodeQuizQuestionVO> questions) {
        this.questions = questions;
    }

    public List<NodeQuizAnswerDTO> getAnswers() {
        return answers;
    }

    public void setAnswers(List<NodeQuizAnswerDTO> answers) {
        this.answers = answers;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }
}