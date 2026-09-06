package com.nexora.mappers;

import com.nexora.entity.query.PracticeReviewQuery;
import com.nexora.entity.vo.PracticeReviewItemVO;
import com.nexora.entity.vo.PracticeReviewStatsVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 答题批阅 数据库操作接口（主观题人工批阅）
 */
public interface PracticeReviewMapper {

    /**
     * 批阅列表（多表聚合：作答流水 + 学生 + 题目 + 知识点 + 课时）
     */
    List<PracticeReviewItemVO> selectReviewList(@Param("query") PracticeReviewQuery query);

    /**
     * 批阅列表计数
     */
    Integer selectReviewCount(@Param("query") PracticeReviewQuery query);

    /**
     * 批阅统计
     */
    PracticeReviewStatsVO selectReviewStats();
}
