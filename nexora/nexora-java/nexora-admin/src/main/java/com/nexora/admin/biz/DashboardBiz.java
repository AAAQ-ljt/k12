package com.nexora.admin.biz;

import com.nexora.entity.vo.DashboardOverviewVO;
import com.nexora.entity.vo.DashboardSessionItemVO;
import com.nexora.entity.vo.DashboardStageItemVO;
import com.nexora.entity.vo.DashboardTodoItemVO;
import com.nexora.entity.vo.DashboardTrendItemVO;
import com.nexora.entity.vo.DashboardUsageTrendItemVO;
import com.nexora.mappers.DashboardMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 控制面板业务：聚合用户/知识库/对话等统计（只读聚合，无循环查库）
 */
@Service
public class DashboardBiz {

    private static final DateTimeFormatter TREND_LABEL = DateTimeFormatter.ofPattern("MM-dd");

    @Resource
    private DashboardMapper dashboardMapper;

    public DashboardOverviewVO overview() {
        DashboardOverviewVO vo = new DashboardOverviewVO();
        vo.setUserCount(safe(dashboardMapper.selectUserCount()));
        vo.setTodayActiveCount(safe(dashboardMapper.selectTodayActiveCount()));
        vo.setKnowledgeDocCount(safe(dashboardMapper.selectKnowledgeDocCount()));
        vo.setAiMessageCount(safe(dashboardMapper.selectAiMessageCount()));
        vo.setTotalTokenCount(safe(dashboardMapper.selectTotalTokenCount()));
        vo.setImageGenCount(safe(dashboardMapper.selectImageGenCount()));
        vo.setUsageTrend(buildUsageTrend());
        vo.setTrend(buildTrend());
        vo.setStageDist(dashboardMapper.selectStageDistribution());
        vo.setRecentSessions(buildRecentSessions());
        vo.setTodos(buildTodos());
        return vo;
    }

    /** 近 7 天趋势补零：查询返回的天没有数据的补齐 0，保证折线图连续 */
    private List<DashboardTrendItemVO> buildTrend() {
        Map<String, Long> byDay = new LinkedHashMap<>();
        for (DashboardTrendItemVO item : dashboardMapper.selectTrend7d()) {
            byDay.put(item.getDayLabel(), item.getCount() == null ? 0L : item.getCount());
        }
        LocalDate today = LocalDate.now();
        List<DashboardTrendItemVO> result = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            String label = today.minusDays(i).format(TREND_LABEL);
            DashboardTrendItemVO item = new DashboardTrendItemVO();
            item.setDayLabel(label);
            item.setCount(byDay.getOrDefault(label, 0L));
            result.add(item);
        }
        return result;
    }

    /** 近 7 天 AI 消耗趋势补零：与对话趋势同口径，缺失天补 0 保证柱状图连续 */
    private List<DashboardUsageTrendItemVO> buildUsageTrend() {
        Map<String, DashboardUsageTrendItemVO> byDay = new LinkedHashMap<>();
        for (DashboardUsageTrendItemVO item : dashboardMapper.selectUsageTrend7d()) {
            byDay.put(item.getDayLabel(), item);
        }
        LocalDate today = LocalDate.now();
        List<DashboardUsageTrendItemVO> result = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            String label = today.minusDays(i).format(TREND_LABEL);
            DashboardUsageTrendItemVO item = byDay.get(label);
            if (item == null) {
                item = new DashboardUsageTrendItemVO();
                item.setDayLabel(label);
                item.setPromptTokens(0L);
                item.setCompletionTokens(0L);
                item.setImageCount(0L);
            }
            result.add(item);
        }
        return result;
    }

    /** 最近会话兜底（为空时返回空列表，前端显示空态） */
    private List<DashboardSessionItemVO> buildRecentSessions() {
        List<DashboardSessionItemVO> sessions = dashboardMapper.selectRecentSessions();
        return sessions == null ? new ArrayList<>() : sessions;
    }

    /** 待办清单：待审核题目 / 主观题待批阅 / 入库失败文档 / 处理中文档 */
    private List<DashboardTodoItemVO> buildTodos() {
        List<DashboardTodoItemVO> todos = new ArrayList<>();
        todos.add(todo("pendingQuestions", "待审核题目", safe(dashboardMapper.selectPendingQuestionCount())));
        todos.add(todo("pendingReviews", "主观题待批阅", safe(dashboardMapper.selectPendingReviewCount())));
        todos.add(todo("failedDocs", "入库失败文档", safe(dashboardMapper.selectFailedDocCount())));
        todos.add(todo("processingDocs", "入库处理中文档", safe(dashboardMapper.selectProcessingDocCount())));
        return todos;
    }

    private DashboardTodoItemVO todo(String key, String label, Long count) {
        DashboardTodoItemVO item = new DashboardTodoItemVO();
        item.setKey(key);
        item.setLabel(label);
        item.setCount(count);
        return item;
    }

    private Long safe(Long value) {
        return value == null ? 0L : value;
    }
}