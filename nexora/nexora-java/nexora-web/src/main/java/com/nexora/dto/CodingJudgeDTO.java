package com.nexora.dto;

/**
 * 编程题判分入参：只上报「运行输出」，预期输出/关键词留在服务端比对。
 *
 * contestId 非空表示比赛模式（服务端会校验该题属于这场比赛）；
 * 学段不取客户端传参，一律用登录态里的学段。
 */
public class CodingJudgeDTO {

    private String problemId;

    private String output;

    /** 比赛模式下的比赛 ID（练习模式为空） */
    private String contestId;

    public String getProblemId() {
        return problemId;
    }

    public void setProblemId(String problemId) {
        this.problemId = problemId;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getContestId() {
        return contestId;
    }

    public void setContestId(String contestId) {
        this.contestId = contestId;
    }
}
