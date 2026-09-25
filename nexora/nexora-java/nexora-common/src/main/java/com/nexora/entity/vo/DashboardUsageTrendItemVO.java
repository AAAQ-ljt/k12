package com.nexora.entity.vo;

/**
 * 控制面板：近 7 天 AI 消耗趋势项
 */
public class DashboardUsageTrendItemVO {

    /** 日期标签 MM-dd */
    private String dayLabel;

    /** 当日输入 token 累计 */
    private Long promptTokens;

    /** 当日输出 token 累计 */
    private Long completionTokens;

    /** 当日图片生成张数 */
    private Long imageCount;

    public String getDayLabel() {
        return dayLabel;
    }

    public void setDayLabel(String dayLabel) {
        this.dayLabel = dayLabel;
    }

    public Long getPromptTokens() {
        return promptTokens;
    }

    public void setPromptTokens(Long promptTokens) {
        this.promptTokens = promptTokens;
    }

    public Long getCompletionTokens() {
        return completionTokens;
    }

    public void setCompletionTokens(Long completionTokens) {
        this.completionTokens = completionTokens;
    }

    public Long getImageCount() {
        return imageCount;
    }

    public void setImageCount(Long imageCount) {
        this.imageCount = imageCount;
    }
}
