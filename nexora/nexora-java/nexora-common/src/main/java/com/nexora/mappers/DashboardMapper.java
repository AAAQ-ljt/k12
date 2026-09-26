package com.nexora.mappers;

import com.nexora.entity.vo.DashboardSessionItemVO;
import com.nexora.entity.vo.DashboardStageItemVO;
import com.nexora.entity.vo.DashboardTrendItemVO;
import com.nexora.entity.vo.DashboardUsageTrendItemVO;

import java.util.List;

/**
 * 控制面板统计查询
 */
public interface DashboardMapper {

    Long selectUserCount();

    Long selectKnowledgeDocCount();

    Long selectTodayActiveCount();

    Long selectAiMessageCount();

    Long selectTotalTokenCount();

    Long selectImageGenCount();

    Long selectTtsGenCount();

    List<DashboardUsageTrendItemVO> selectUsageTrend7d();

    List<DashboardTrendItemVO> selectTrend7d();

    List<DashboardStageItemVO> selectStageDistribution();

    List<DashboardSessionItemVO> selectRecentSessions();

    Long selectPendingQuestionCount();

    Long selectPendingReviewCount();

    Long selectFailedDocCount();

    Long selectProcessingDocCount();
}