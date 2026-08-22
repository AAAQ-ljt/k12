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
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
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

    @Resource
    private ChatClient chatClient;

    @Value("${spring.ai.openai.chat.options.model:deepseek-v4-flash}")
    private String chatModel;

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

    /**
     * AI 学习报告（主线 9 最小主体）：基于六类学习统计生成自然语言 Markdown 报告
     */
    public String aiReport(String userId) {
        LearningUserDetailVO detail = userDetail(userId);
        String input = buildReportInput(detail);
        String systemPrompt = """
                你是 K12 人工智能通识课的班主任 AI 助教，负责为学生撰写个性化学习报告。
                根据提供的学生学习统计数据，输出一份 Markdown 学习报告，结构：
                ## 总体评价(2-3 句)
                ## 学习亮点(分点列出 2-4 条真实数据亮点)
                ## 薄弱环节(分点指出 2-3 个需加强的方向)
                ## 针对性建议(按学段给出 3-5 条可执行建议，结合 AI 助教/动画/绘本/编程/练习等学习方式)
                ## 下一步行动(3 条具体、可立刻开始)
                要求：只使用给定数据，不编造；语气鼓励、温和；面向学生本人，语言通俗。""";
        try {
            String content = chatClient.prompt()
                    .system(systemPrompt)
                    .user(input)
                    .options(OpenAiChatOptions.builder().model(chatModel).build())
                    .call()
                    .content();
            if (content == null || content.isBlank()) {
                throw new BusinessException("AI 报告生成失败，请稍后重试");
            }
            return content.trim();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("AI 报告生成失败，请稍后重试");
        }
    }

    private String buildReportInput(LearningUserDetailVO detail) {
        StringBuilder builder = new StringBuilder();
        UserInfo user = detail.getUserInfo();
        builder.append("学生：").append(user == null ? "-" : user.getNickName() != null ? user.getNickName() : user.getUsername())
                .append("，学段：").append(user == null || user.getStage() == null ? "-" : user.getStage())
                .append("，年级：").append(user == null || user.getGrade() == null ? "-" : user.getGrade()).append("\n");
        builder.append("- 课程：已学 ").append(nz(detail.getCourseCount())).append(" 门，完成 ").append(nz(detail.getCourseFinishedCount()))
                .append(" 门，平均进度 ").append(detail.getCourseAvgProgress() == null ? 0 : detail.getCourseAvgProgress()).append("%\n");
        builder.append("- 练习：共 ").append(nz(detail.getPracticeCount())).append(" 次，正确 ").append(nz(detail.getPracticeCorrectCount()))
                .append(" 次，正确率 ").append(percent(detail.getPracticeAccuracy())).append("\n");
        builder.append("- 个人知识库：资源 ").append(nz(detail.getWikiResourceCount())).append(" 个，占用 ")
                .append(detail.getWikiResourceUsedMb() == null ? "0" : String.format("%.1f", detail.getWikiResourceUsedMb()))
                .append("MB / 配额 ").append(detail.getWikiQuotaPercent() == null ? "0" : String.format("%.0f", detail.getWikiQuotaPercent()))
                .append("%\n");
        builder.append("- AI 对话：会话 ").append(nz(detail.getAiSessionCount())).append(" 个，消息 ")
                .append(nz(detail.getAiMessageCount())).append(" 条，Token 合计 ").append(nz(detail.getAiTokenCount())).append("\n");
        builder.append("- 掌握度：平均分 ").append(detail.getMasteryAvgScore() == null ? 0 : detail.getMasteryAvgScore())
                .append("，已掌握知识点 ").append(detail.getMasteryMasteredCount() == null ? 0 : detail.getMasteryMasteredCount()).append(" 个\n");
        if (detail.getCourseList() != null && !detail.getCourseList().isEmpty()) {
            builder.append("课程明细：\n");
            for (CourseStudyProgressItemVO item : detail.getCourseList()) {
                builder.append("  - ").append(item.getCourseName()).append("：进度 ")
                        .append(item.getProgress() == null ? 0 : item.getProgress()).append("%\n");
            }
        }
        if (detail.getPracticeKnowledgePoints() != null && !detail.getPracticeKnowledgePoints().isEmpty()) {
            builder.append("练习知识点：\n");
            for (PracticeKnowledgePointVO item : detail.getPracticeKnowledgePoints()) {
                builder.append("  - ").append(item.getKnowledgePointName()).append("：练习 ")
                        .append(nz(item.getPracticeCount())).append(" 次，正确率 ").append(percent(item.getAccuracy())).append("\n");
            }
        }
        if (detail.getMasteryList() != null && !detail.getMasteryList().isEmpty()) {
            builder.append("掌握度明细：\n");
            for (KnowledgeMasteryVO item : detail.getMasteryList()) {
                builder.append("  - ").append(item.getKnowledgePointName()).append("：得分 ")
                        .append(item.getMasteryScore() == null ? 0 : item.getMasteryScore()).append("\n");
            }
        }
        return builder.toString();
    }

    private long nz(Long value) {
        return value == null ? 0L : value;
    }

    private String percent(Double value) {
        if (value == null) {
            return "0%";
        }
        return String.format("%.0f%%", value * 100);
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
        // 两段式后统计口径：仅「已确认入库」的知识页（vectorStatus=2）计入已向量化文档
        query.setVectorStatus(2);
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
