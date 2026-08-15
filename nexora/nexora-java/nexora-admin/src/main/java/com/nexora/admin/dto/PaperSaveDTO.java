package com.nexora.admin.dto;

import com.nexora.entity.po.PaperInfo;

import java.util.List;

/**
 * 试卷保存 DTO：试卷信息 + 大题 + 题目
 */
public class PaperSaveDTO {

    private PaperInfo paper;

    private List<GroupItem> groups;

    public PaperInfo getPaper() {
        return paper;
    }

    public void setPaper(PaperInfo paper) {
        this.paper = paper;
    }

    public List<GroupItem> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupItem> groups) {
        this.groups = groups;
    }

    public static class GroupItem {

        private String groupId;

        private String groupName;

        private List<QuestionItem> questions;

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

        public List<QuestionItem> getQuestions() {
            return questions;
        }

        public void setQuestions(List<QuestionItem> questions) {
            this.questions = questions;
        }
    }

    public static class QuestionItem {

        private String questionId;

        private Integer score;

        public String getQuestionId() {
            return questionId;
        }

        public void setQuestionId(String questionId) {
            this.questionId = questionId;
        }

        public Integer getScore() {
            return score;
        }

        public void setScore(Integer score) {
            this.score = score;
        }
    }
}
