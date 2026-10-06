package com.nexora.dto;

/**
 * 编程题判分入参：只上报「运行输出」，预期输出/关键词留在服务端比对
 */
public class CodingJudgeDTO {

    private String problemId;

    private String output;

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
}
