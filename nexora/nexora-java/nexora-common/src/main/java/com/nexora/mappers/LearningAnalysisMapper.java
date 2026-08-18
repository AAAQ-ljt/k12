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
}
