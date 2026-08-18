package com.nexora.admin.biz;

import com.nexora.admin.dto.KnowledgeSearchTestRequest;
import com.nexora.admin.vo.KnowledgeSearchResultVO;
import com.nexora.constants.Constants;
import com.nexora.entity.enums.PageSize;
import com.nexora.entity.po.KnowledgeDoc;
import com.nexora.entity.po.UserInfo;
import com.nexora.entity.query.KnowledgeDocQuery;
import com.nexora.entity.query.LearningUserQuery;
import com.nexora.entity.query.SimplePage;
import com.nexora.entity.vo.AiIntentVO;
import com.nexora.entity.vo.AiRecentMessageVO;
import com.nexora.entity.vo.CourseStudyProgressItemVO;
import com.nexora.entity.vo.KnowledgeMasteryVO;
import com.nexora.entity.vo.KnowledgeResourceVO;
import com.nexora.entity.vo.LearningOverviewVO;
import com.nexora.entity.vo.LearningUserDetailVO;
import com.nexora.entity.vo.LearningUserSummaryVO;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.vo.PracticeKnowledgePointVO;
import com.nexora.entity.vo.PracticeQuestionTypeVO;
import com.nexora.exception.BusinessException;
import com.nexora.mappers.LearningAnalysisMapper;
import com.nexora.service.KnowledgeDocService;
import com.nexora.service.UserInfoService;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 学习分析业务：总览与学生个人学习情况聚合。
 */
@Service
public class LearningAnalysisBiz {

    @Resource
    private LearningAnalysisMapper learningAnalysisMapper;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private KnowledgeDocService knowledgeDocService;

    @Resource
    private KnowledgeBaseBiz knowledgeBaseBiz;

    public LearningOverviewVO overview() {
        return learningAnalysisMapper.selectOverview();
    }

    public PaginationResultVO<LearningUserSummaryVO> userPage(LearningUserQuery query) {
        query.setRoleType(Constants.ROLE_STUDENT);
        int count = learningAnalysisMapper.selectUserCount(query);
        int pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
        query.setSimplePage(page);
        List<LearningUserSummaryVO> list = learningAnalysisMapper.selectUserList(query);
        return new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
    }

    public LearningUserDetailVO userDetail(String userId) {
        UserInfo userInfo = validateStudent(userId);

        LearningUserDetailVO detail = learningAnalysisMapper.selectUserDetail(userId);
        detail.setUserInfo(userInfo);
        detail.setCourseList(learningAnalysisMapper.selectCourseProgressList(userId));
        detail.setPracticeKnowledgePoints(learningAnalysisMapper.selectPracticeKnowledgePointList(userId));
        detail.setPracticeQuestionTypes(learningAnalysisMapper.selectPracticeQuestionTypeList(userId));
        detail.setKnowledgeResources(learningAnalysisMapper.selectKnowledgeResourceList(userId));
        detail.setKnowledgeResourceTypes(learningAnalysisMapper.selectKnowledgeResourceTypeList(userId));
        List<AiIntentVO> intents = learningAnalysisMapper.selectAiIntentList(userId);
        List<AiRecentMessageVO> recentMessages = learningAnalysisMapper.selectAiRecentMessageList(userId);
        List<KnowledgeMasteryVO> masteryList = learningAnalysisMapper.selectMasteryList(userId);

        detail.setAiIntents(intents);
        detail.setAiRecentMessages(recentMessages);
        detail.setMasteryList(masteryList);

        if (detail.getKnowledgeResources() == null || detail.getKnowledgeResources().isEmpty()) {
            detail.setKnowledgeResources(new java.util.ArrayList<>());
        }
        if (detail.getKnowledgeResourceTypes() == null || detail.getKnowledgeResourceTypes().isEmpty()) {
            detail.setKnowledgeResourceTypes(new java.util.ArrayList<>());
        }
        if (intents == null || intents.isEmpty()) {
            detail.setAiIntents(new java.util.ArrayList<>());
        }
        if (recentMessages == null || recentMessages.isEmpty()) {
            detail.setAiRecentMessages(new java.util.ArrayList<>());
        }
        if (masteryList == null || masteryList.isEmpty()) {
            detail.setMasteryList(new java.util.ArrayList<>());
        }
        return detail;
    }

    public PaginationResultVO<KnowledgeDoc> studentDocList(String userId, KnowledgeDocQuery query) {
        validateStudent(userId);
        if (query.getPageNo() == null) {
            query.setPageNo(1);
        }
        if (query.getPageSize() == null) {
            query.setPageSize(10);
        }
        if (StringTools.isEmpty(query.getOrderBy())) {
            query.setOrderBy("create_time desc");
        }
        query.setOwnerId(userId);
        query.setOwnerIdNull(null);
        return knowledgeDocService.findListByPage(query);
    }

    public KnowledgeDoc studentDocDetail(String userId, String docId) {
        validateStudent(userId);
        if (StringTools.isEmpty(docId)) {
            throw new BusinessException("文档ID不能为空");
        }
        KnowledgeDoc doc = knowledgeDocService.getKnowledgeDocByDocId(docId);
        if (doc == null || !userId.equals(doc.getOwnerId())) {
            throw new BusinessException("文档不存在");
        }
        return doc;
    }

    public List<KnowledgeSearchResultVO> studentSearchTest(String userId, KnowledgeSearchTestRequest request) {
        validateStudent(userId);
        if (request == null || StringTools.isEmpty(request.getQuestion())) {
            throw new BusinessException("请输入测试问题");
        }
        request.setOwnerId(userId);
        return knowledgeBaseBiz.searchTest(request);
    }

    private UserInfo validateStudent(String userId) {
        if (StringTools.isEmpty(userId)) {
            throw new BusinessException("用户ID不能为空");
        }
        UserInfo userInfo = userInfoService.getUserInfoByUserId(userId);
        if (userInfo == null || !Constants.ROLE_STUDENT.equals(userInfo.getRoleType())) {
            throw new BusinessException("学生不存在");
        }
        return userInfo;
    }
}
