package com.nexora.vo;

import java.util.List;

/**
 * 节点快测出题响应：节点 + 题目（含答案与解析）。
 *
 * 快测为「路由节点自己出题自测」的闭环；题目由 AI 依据节点知识点现场生成，不依赖课程题库。
 * 前端答题阶段不展示 answer/analysis，仅在提交判分结果时回显解析。
 */
public class NodeQuizVO {

    /** 节点ID */
    private String itemId;

    /** 知识点ID */
    private String knowledgePointId;

    /** 知识点/主题名 */
    private String knowledgePointName;

    /** 测验标题 */
    private String title;

    /** 题目列表（含答案与解析，供提交时回传判分） */
    private List<NodeQuizQuestionVO> questions;

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getKnowledgePointId() {
        return knowledgePointId;
    }

    public void setKnowledgePointId(String knowledgePointId) {
        this.knowledgePointId = knowledgePointId;
    }

    public String getKnowledgePointName() {
        return knowledgePointName;
    }

    public void setKnowledgePointName(String knowledgePointName) {
        this.knowledgePointName = knowledgePointName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<NodeQuizQuestionVO> getQuestions() {
        return questions;
    }

    public void setQuestions(List<NodeQuizQuestionVO> questions) {
        this.questions = questions;
    }

    public static class NodeQuizQuestionVO {

        /** 题号（0 起） */
        private int index;

        /** 题型（SINGLE 单选） */
        private String type;

        /** 题干 */
        private String question;

        /** 选项文本列表（如 ["A. 内容", ...]） */
        private List<String> options;

        /** 正确选项下标（0 起，判分依据） */
        private int answer;

        /** 解析 */
        private String analysis;

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public List<String> getOptions() {
            return options;
        }

        public void setOptions(List<String> options) {
            this.options = options;
        }

        public int getAnswer() {
            return answer;
        }

        public void setAnswer(int answer) {
            this.answer = answer;
        }

        public String getAnalysis() {
            return analysis;
        }

        public void setAnalysis(String analysis) {
            this.analysis = analysis;
        }
    }
}