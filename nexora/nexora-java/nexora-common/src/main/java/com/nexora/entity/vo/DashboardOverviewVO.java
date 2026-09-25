package com.nexora.entity.vo;

import java.util.List;

/**
 * 管理端控制面板总览数据
 */
public class DashboardOverviewVO {

    /** 学生总数 */
    private Long userCount;

    /** 今日活跃（今日有话对话过的学生数） */
    private Long todayActiveCount;

    /** 官方知识库文档数 */
    private Long knowledgeDocCount;

    /** AI 对话总条数（用户消息） */
    private Long aiMessageCount;

    /** Token 总消耗（全部大模型调用输入+输出累计） */
    private Long totalTokenCount;

    /** 图片生成张数（文生图累计，供应商一次调用出一张） */
    private Long imageGenCount;

    /** 近 7 天 AI 消耗趋势（token 与生图张数） */
    private List<DashboardUsageTrendItemVO> usageTrend;

    /** 近 7 天 AI 对话趋势 */
    private List<DashboardTrendItemVO> trend;

    /** 学段用户分布 */
    private List<DashboardStageItemVO> stageDist;

    /** 最近 AI 会话 */
    private List<DashboardSessionItemVO> recentSessions;

    /** 待办事项 */
    private List<DashboardTodoItemVO> todos;

    public Long getUserCount() {
        return userCount;
    }

    public void setUserCount(Long userCount) {
        this.userCount = userCount;
    }

    public Long getTodayActiveCount() {
        return todayActiveCount;
    }

    public void setTodayActiveCount(Long todayActiveCount) {
        this.todayActiveCount = todayActiveCount;
    }

    public Long getKnowledgeDocCount() {
        return knowledgeDocCount;
    }

    public void setKnowledgeDocCount(Long knowledgeDocCount) {
        this.knowledgeDocCount = knowledgeDocCount;
    }

    public Long getAiMessageCount() {
        return aiMessageCount;
    }

    public void setAiMessageCount(Long aiMessageCount) {
        this.aiMessageCount = aiMessageCount;
    }

    public Long getTotalTokenCount() {
        return totalTokenCount;
    }

    public void setTotalTokenCount(Long totalTokenCount) {
        this.totalTokenCount = totalTokenCount;
    }

    public Long getImageGenCount() {
        return imageGenCount;
    }

    public void setImageGenCount(Long imageGenCount) {
        this.imageGenCount = imageGenCount;
    }

    public List<DashboardUsageTrendItemVO> getUsageTrend() {
        return usageTrend;
    }

    public void setUsageTrend(List<DashboardUsageTrendItemVO> usageTrend) {
        this.usageTrend = usageTrend;
    }

    public List<DashboardTrendItemVO> getTrend() {
        return trend;
    }

    public void setTrend(List<DashboardTrendItemVO> trend) {
        this.trend = trend;
    }

    public List<DashboardStageItemVO> getStageDist() {
        return stageDist;
    }

    public void setStageDist(List<DashboardStageItemVO> stageDist) {
        this.stageDist = stageDist;
    }

    public List<DashboardSessionItemVO> getRecentSessions() {
        return recentSessions;
    }

    public void setRecentSessions(List<DashboardSessionItemVO> recentSessions) {
        this.recentSessions = recentSessions;
    }

    public List<DashboardTodoItemVO> getTodos() {
        return todos;
    }

    public void setTodos(List<DashboardTodoItemVO> todos) {
        this.todos = todos;
    }
}