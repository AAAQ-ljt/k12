package com.nexora.admin.vo;

import com.nexora.entity.po.PaperInfo;

import java.util.List;

/**
 * 试卷详情 VO：试卷信息 + 大题 + 题目预览
 */
public class PaperDetailVO {

    private PaperInfo paper;

    private List<GroupVO> groups;

    public PaperInfo getPaper() {
        return paper;
    }

    public void setPaper(PaperInfo paper) {
        this.paper = paper;
    }

    public List<GroupVO> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupVO> groups) {
        this.groups = groups;
    }

    public static class GroupVO {

        private String groupId;

        private String groupName;

        private List<QuestionVO> questions;

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }

        public List<QuestionVO> getQuestions() {
            return questions;
        }

        public void setQuestions(List<QuestionVO> questions) {
            this.questions = questions;
        }
    }

    public static class QuestionVO {

        private String questionId;

        private String title;

        private Integer questionType;

        private Integer difficulty;

        private Integer score;

        private Integer sort;

        public String getQuestionId() {
            return questionId;
        }

        public void setQuestionId(String questionId) {
            this.questionId = questionId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Integer getQuestionType() {
            return questionType;
        }

        public void setQuestionType(Integer questionType) {
            this.questionType = questionType;
        }

        public Integer getDifficulty() {
            return difficulty;
        }

        public void setDifficulty(Integer difficulty) {
            this.difficulty = difficulty;
        }

        public Integer getScore() {
            return score;
        }

        public void setScore(Integer score) {
            this.score = score;
        }

        public Integer getSort() {
            return sort;
        }

        public void setSort(Integer sort) {
            this.sort = sort;
        }
    }
}
