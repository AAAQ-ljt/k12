package com.nexora.mappers;

import com.nexora.entity.query.LearningUserQuery;
import com.nexora.entity.vo.AiIntentVO;
import com.nexora.entity.vo.AiRecentMessageVO;
import com.nexora.entity.vo.CourseStudyProgressItemVO;
import com.nexora.entity.vo.KnowledgeMasteryVO;
import com.nexora.entity.vo.KnowledgeResourceVO;
import com.nexora.entity.vo.LearningOverviewVO;
import com.nexora.entity.vo.LearningUserDetailVO;
import com.nexora.entity.vo.LearningUserSummaryVO;
import com.nexora.entity.vo.PracticeKnowledgePointVO;
import com.nexora.entity.vo.PracticeQuestionTypeVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 学习分析统计查询
 */
public interface LearningAnalysisMapper {

    LearningOverviewVO selectOverview();

    List<LearningUserSummaryVO> selectUserList(@Param("query") LearningUserQuery query);

    Integer selectUserCount(@Param("query") LearningUserQuery query);

    LearningUserDetailVO selectUserDetail(@Param("userId") String userId);

    List<CourseStudyProgressItemVO> selectCourseProgressList(@Param("userId") String userId);

    List<PracticeKnowledgePointVO> selectPracticeKnowledgePointList(@Param("userId") String userId);

    List<PracticeQuestionTypeVO> selectPracticeQuestionTypeList(@Param("userId") String userId);

    List<KnowledgeResourceVO> selectKnowledgeResourceTypeList(@Param("userId") String userId);

    List<KnowledgeResourceVO> selectKnowledgeResourceList(@Param("userId") String userId);

    List<AiIntentVO> selectAiIntentList(@Param("userId") String userId);

    List<AiRecentMessageVO> selectAiRecentMessageList(@Param("userId") String userId);

    List<KnowledgeMasteryVO> selectMasteryList(@Param("userId") String userId);

    /**
     * 按学段过滤的掌握度查询（MCP queryMastery 工具用）：stage 来自学生登录档案注入，
     * 与 selectMasteryList 同结构，仅多一个学段条件；admin 侧既有调用方不受影响
     */
    List<KnowledgeMasteryVO> selectMasteryListByStage(@Param("userId") String userId, @Param("stage") String stage);
}
