package com.nexora.dto;

import java.util.List;

/**
 * 课时通关测验提交请求
 */
public class LessonQuizSubmitDTO {

    /** 课时ID */
    private String lessonId;

    /** 作答列表（须覆盖测验全部题目） */
    private List<LessonQuizAnswerDTO> answers;

    /** 答题用时（秒，可选，缺省按 0 记录） */
    private Integer duration;

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public List<LessonQuizAnswerDTO> getAnswers() {
        return answers;
    }

    public void setAnswers(List<LessonQuizAnswerDTO> answers) {
        this.answers = answers;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }
}