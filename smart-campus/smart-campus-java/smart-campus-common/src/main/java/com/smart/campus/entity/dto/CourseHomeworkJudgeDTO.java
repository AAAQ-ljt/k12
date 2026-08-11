package com.smart.campus.entity.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CourseHomeworkJudgeDTO implements Serializable {

    @NotNull(message = "提交记录ID不能为空")
    private Long submitId;

    private String teacherComment;

    @Valid
    private List<CourseHomeworkJudgeQuestionDTO> questionScoreList = new ArrayList<>();

    public Long getSubmitId() {
        return submitId;
    }

    public void setSubmitId(Long submitId) {
        this.submitId = submitId;
    }

    public String getTeacherComment() {
        return teacherComment;
    }

    public void setTeacherComment(String teacherComment) {
        this.teacherComment = teacherComment;
    }

    public List<CourseHomeworkJudgeQuestionDTO> getQuestionScoreList() {
        return questionScoreList;
    }

    public void setQuestionScoreList(List<CourseHomeworkJudgeQuestionDTO> questionScoreList) {
        this.questionScoreList = questionScoreList;
    }
}
