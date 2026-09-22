package com.nexora.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.nexora.component.KnowledgeMasteryComponent;
import com.nexora.component.LearningPathComponent;
import com.nexora.component.LearningPathGenerateComponent;
import com.nexora.dto.NodeQuizAnswerDTO;
import com.nexora.dto.NodeQuizSubmitDTO;
import com.nexora.entity.enums.DateTimePatternEnum;
import com.nexora.entity.po.AiGenerationRecord;
import com.nexora.entity.po.KnowledgeDoc;
import com.nexora.entity.po.KnowledgeMastery;
import com.nexora.entity.po.KnowledgePoint;
import com.nexora.entity.po.LearningPath;
import com.nexora.entity.po.LearningPathItem;
import com.nexora.entity.po.PracticeRecord;
import com.nexora.entity.po.UserWikiProfile;
import com.nexora.entity.query.AiGenerationRecordQuery;
import com.nexora.entity.query.KnowledgeDocQuery;
import com.nexora.entity.query.KnowledgeMasteryQuery;
import com.nexora.entity.query.KnowledgePointQuery;
import com.nexora.exception.BusinessException;
import com.nexora.service.AiGenerationRecordService;
import com.nexora.service.KnowledgeDocService;
import com.nexora.service.KnowledgeMasteryService;
import com.nexora.service.KnowledgePointService;
import com.nexora.service.LearningPathItemService;
import com.nexora.service.PracticeRecordService;
import com.nexora.service.StudentLearningPathService;
import com.nexora.service.UserWikiProfileService;
import com.nexora.utils.DateUtil;
import com.nexora.utils.StringTools;
import com.nexora.vo.LearningPathBranchVO;
import com.nexora.vo.LearningPathNodeVO;
import com.nexora.vo.LearningPathStageVO;
import com.nexora.vo.LearningPathSummaryVO;
import com.nexora.vo.LearningPathVO;
import com.nexora.vo.NodeQuizSubmitResultVO;
import com.nexora.vo.NodeQuizVO;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 学生个性化学习路径业务实现（方案 A）：
 * 学习档案 + 已学内容 → AI 产出「叙事层（目标/阶段/任务）」→
 * 结构层落 learning_path / learning_path_item（含自动建点），叙事层 JSON 落 ai_generation_record.content（带 pathId）；
 * 读取时按 pathId 取叙事层、按知识点名与节点对齐；节点三态由掌握度驱动（LearningPathComponent）。
 */
@Service
public class StudentLearningPathServiceImpl implements StudentLearningPathService {

    private static final Logger log = LoggerFactory.getLogger(StudentLearningPathServiceImpl.class);

    /** 生成记录类型 */
    private static final String RECORD_TYPE = "LEARNING_PATH";

    /** 知识页向量状态：已确认入库（视为已学） */
    private static final int VECTOR_STATUS_CONFIRMED = 2;

    /** 节点快测单题满分（客观题答对记 1 分） */
    private static final int NODE_QUIZ_QUESTION_SCORE = 1;

    /** 练习流水来源：路径快测 */
    private static final int PRACTICE_SOURCE_PATH_QUIZ = 1;

    /** 练习流水批阅状态：客观题无需批阅 */
    private static final int REVIEW_STATUS_UNNEEDED = 2;

    /** 题型：单选 */
    private static final int QUESTION_TYPE_SINGLE = 0;

    @Resource
    private LearningPathItemService learningPathItemService;

    @Resource
    private PracticeRecordService practiceRecordService;

    @Resource
    private KnowledgeMasteryComponent knowledgeMasteryComponent;

    @Resource
    private AiGenerationRecordService aiGenerationRecordService;

    @Resource
    private UserWikiProfileService userWikiProfileService;

    @Resource
    private KnowledgeDocService knowledgeDocService;

    @Resource
    private KnowledgeMasteryService knowledgeMasteryService;

    @Resource
    private KnowledgePointService knowledgePointService;

    @Resource
    private LearningPathGenerateComponent learningPathGenerateComponent;

    @Resource
    private LearningPathComponent learningPathComponent;

    @Override
    public LearningPathVO generate(String userId, String stage) {
        UserWikiProfile profile = userWikiProfileService.getUserWikiProfileByUserId(userId);
        if (profile == null || StringTools.isEmpty(profile.getLearningGoal())) {
            // 拦门：没有学习目标时模型只能凭教材/学段自由发挥，产出很泛，先引导填写档案
            throw new BusinessException("请先填写「学习目标」，AI 才能规划出贴合你的学习路径");
        }
        String profileText = buildProfileText(profile);
        List<String> learnedTitles = listLearnedTitles(userId);
        LearningPathGenerateComponent.LearningPathPlan plan =
                learningPathGenerateComponent.generate(stage, profileText, learnedTitles);

        List<LearningPathComponent.NodeDraft> mainLine = new ArrayList<>();
        for (LearningPathGenerateComponent.PlanNode node : plan.flatMainLine()) {
            mainLine.add(new LearningPathComponent.NodeDraft(node.title(), node.task(), node.kind()));
        }
        List<LearningPathComponent.BranchDraft> branches = new ArrayList<>();
        for (LearningPathGenerateComponent.BranchDraft branch : plan.branches()) {
            List<LearningPathComponent.NodeDraft> nodes = new ArrayList<>();
            for (LearningPathGenerateComponent.PlanNode node : branch.nodes()) {
                nodes.add(new LearningPathComponent.NodeDraft(node.title(), node.task(), node.kind()));
            }
            branches.add(new LearningPathComponent.BranchDraft(branch.name(), nodes));
        }
        String pathId = learningPathComponent.createPath(userId, stage, plan.title(), mainLine, branches);
        saveNarrative(userId, stage, plan, pathId);
        log.info("学习路径生成完成 userId={} pathId={} 阶段={} 主线节点={} 分支={}",
                userId, pathId, plan.stages().size(), mainLine.size(), branches.size());
        return detail(userId, pathId);
    }

    @Override
    public List<LearningPathSummaryVO> myList(String userId) {
        List<LearningPathComponent.PathWithItems> paths = learningPathComponent.listMyPaths(userId);
        if (paths.isEmpty()) {
            return new ArrayList<>();
        }
        Map<String, Narrative> narrativeMap = loadNarrativeMap(userId);
        List<LearningPathSummaryVO> result = new ArrayList<>();
        for (LearningPathComponent.PathWithItems path : paths) {
            LearningPathSummaryVO vo = new LearningPathSummaryVO();
            LearningPath dbPath = path.path();
            vo.setPathId(dbPath.getPathId());
            vo.setTitle(dbPath.getTitle());
            vo.setStage(dbPath.getStage());
            vo.setStatus(dbPath.getStatus());
            vo.setTotalItems(dbPath.getTotalItems());
            vo.setFinishedItems(dbPath.getFinishedItems());
            vo.setProgress(dbPath.getProgress());
            vo.setCreateTime(formatTime(dbPath.getCreateTime()));
            vo.setUpdateTime(formatTime(dbPath.getUpdateTime()));
            Narrative narrative = narrativeMap.get(dbPath.getPathId());
            vo.setLegacy(narrative == null);
            vo.setStageCount(narrative == null ? null : narrative.stages().size());
            vo.setGoal(narrative == null ? null : narrative.goal());
            vo.setCurrentNodeName(currentNodeName(dbPath, path.items()));
            result.add(vo);
        }
        return result;
    }

    @Override
    public LearningPathVO detail(String userId, String pathId) {
        LearningPathComponent.PathWithItems path = learningPathComponent.getMyPath(userId, pathId);
        Narrative narrative = loadNarrative(userId, pathId);
        Map<String, KnowledgeMastery> masteryMap = loadMasteryMap(userId);
        Map<String, KnowledgePoint> pointMap = loadPointMap();
        return buildDetailVO(path.path(), path.items(), narrative, masteryMap, pointMap);
    }

    @Override
    public void delete(String userId, String pathId) {
        learningPathComponent.deletePath(userId, pathId);
        // 同时清理叙事层快照，避免留下无主记录
        try {
            List<AiGenerationRecord> records = listRecords(userId);
            for (AiGenerationRecord record : records) {
                Narrative narrative = parseNarrative(record.getContent());
                if (narrative != null && pathId.equals(narrative.pathId())) {
                    aiGenerationRecordService.deleteAiGenerationRecordByRecordId(record.getRecordId());
                }
            }
        } catch (Exception e) {
            log.warn("清理学习路径叙事层快照失败 pathId={}", pathId, e);
        }
    }

    @Override
    public List<AiGenerationRecord> historyList(String userId) {
        // 只保留旧版记录（无 pathId 的生成记录）；带 pathId 的属于在用路线，不进历史
        List<AiGenerationRecord> result = new ArrayList<>();
        for (AiGenerationRecord record : listRecords(userId)) {
            Narrative narrative = parseNarrative(record.getContent());
            if (narrative == null || StringTools.isEmpty(narrative.pathId())) {
                result.add(record);
            }
        }
        return result;
    }

    @Override
    public void deleteHistory(String userId, String recordId) {
        AiGenerationRecord record = aiGenerationRecordService.getAiGenerationRecordByRecordId(recordId);
        if (record == null || !userId.equals(record.getUserId())) {
            throw new BusinessException("学习路径记录不存在或无权操作");
        }
        aiGenerationRecordService.deleteAiGenerationRecordByRecordId(recordId);
    }

    // ==================== 节点快测（路径节点自测闭环） ====================

    @Override
    public NodeQuizSubmitResultVO submitNodeQuiz(String userId, NodeQuizSubmitDTO dto) {
        if (dto == null || StringTools.isEmpty(dto.getItemId())) {
            throw new BusinessException("节点不能为空");
        }
        LearningPathItem item = learningPathItemService.getLearningPathItemByItemId(dto.getItemId());
        if (item == null || !userId.equals(item.getUserId())) {
            throw new BusinessException("节点不存在或无权操作");
        }
        if (item.getStatus() != null && item.getStatus() == LearningPathComponent.ITEM_STATUS_LOCKED) {
            throw new BusinessException("该节点还未解锁，请先完成前置节点");
        }
        LearningPath path = learningPathComponent.requireOwnedPath(userId, item.getPathId());

        // 把提交的题目按题号建索引（判分依据），作答按题号合并
        Map<Integer, NodeQuizVO.NodeQuizQuestionVO> questionMap = new HashMap<>();
        if (dto.getQuestions() != null) {
            for (NodeQuizVO.NodeQuizQuestionVO question : dto.getQuestions()) {
                if (question != null) {
                    questionMap.put(question.getIndex(), question);
                }
            }
        }
        Map<Integer, NodeQuizAnswerDTO> answerMap = new HashMap<>();
        if (dto.getAnswers() != null) {
            for (NodeQuizAnswerDTO answer : dto.getAnswers()) {
                if (answer != null) {
                    answerMap.put(answer.getIndex(), answer);
                }
            }
        }
        if (questionMap.isEmpty()) {
            throw new BusinessException("题目数据缺失，请重新出题作答");
        }

        Date now = new Date();
        List<PracticeRecord> records = new ArrayList<>();
        List<KnowledgeMasteryComponent.AnswerOutcome> outcomes = new ArrayList<>();
        List<NodeQuizSubmitResultVO.QuestionResult> results = new ArrayList<>();
        int correctCount = 0;

        // 服务端权威判分：以题目携带的正确答案比对学生选项，不信任前端自带的判分
        for (Map.Entry<Integer, NodeQuizVO.NodeQuizQuestionVO> entry : questionMap.entrySet()) {
            NodeQuizVO.NodeQuizQuestionVO question = entry.getValue();
            List<String> options = question.getOptions();
            String correctAnswer = (question.getAnswer() >= 0 && options != null && question.getAnswer() < options.size())
                    ? options.get(question.getAnswer()) : "";
            NodeQuizAnswerDTO given = answerMap.get(question.getIndex());
            String userAnswer = given == null ? "" : (given.getUserAnswer() == null ? "" : given.getUserAnswer());
            boolean correct = !StringTools.isEmpty(correctAnswer) && correctAnswer.equals(userAnswer);
            if (correct) {
                correctCount++;
            }

            PracticeRecord record = new PracticeRecord();
            record.setUserId(userId);
            // knowledge_point_id 非空约束：异常缺失时落空串保证流水可写
            record.setKnowledgePointId(StringTools.isEmpty(item.getKnowledgePointId())
                    ? "" : item.getKnowledgePointId());
            record.setStage(path.getStage());
            // LLM 现场出题无题目库身份，questionId 置空串
            record.setQuestionId("");
            record.setQuestionType(QUESTION_TYPE_SINGLE);
            record.setUserAnswer(userAnswer);
            record.setIsCorrect(correct ? 1 : 0);
            record.setScore(correct ? NODE_QUIZ_QUESTION_SCORE : 0);
            record.setDuration(dto.getDuration() == null ? 0 : dto.getDuration());
            record.setSource(PRACTICE_SOURCE_PATH_QUIZ);
            record.setBizId(item.getItemId());
            // 客观题无需人工批阅
            record.setReviewStatus(REVIEW_STATUS_UNNEEDED);
            record.setCreateTime(now);
            records.add(record);
            if (!StringTools.isEmpty(record.getKnowledgePointId())) {
                outcomes.add(new KnowledgeMasteryComponent.AnswerOutcome(record.getKnowledgePointId(), correct));
            }

            NodeQuizSubmitResultVO.QuestionResult result = new NodeQuizSubmitResultVO.QuestionResult();
            result.setIndex(question.getIndex());
            result.setQuestion(question.getQuestion());
            result.setOptions(options);
            result.setUserAnswer(userAnswer);
            result.setCorrectAnswer(correctAnswer);
            result.setCorrect(correct);
            result.setAnalysis(question.getAnalysis());
            results.add(result);
        }

        if (!records.isEmpty()) {
            practiceRecordService.addBatch(records);
        }
        // 掌握度回写（本次得分率达标 + 足够练习次数 → 节点跨入已掌握，闭环即时生效）
        if (!outcomes.isEmpty()) {
            knowledgeMasteryComponent.recordAnswers(userId, path.getStage(), outcomes);
        }
        // 回写后刷新节点三态与路径进度，再取最新掌握度
        learningPathComponent.getMyPath(userId, item.getPathId());
        KnowledgeMastery mastery = StringTools.isEmpty(item.getKnowledgePointId())
                ? null : loadMasteryMap(userId).get(item.getKnowledgePointId());

        int total = results.size();
        int score = total == 0 ? 0 : (int) Math.round(correctCount * 100.0 / total);
        NodeQuizSubmitResultVO resultVO = new NodeQuizSubmitResultVO();
        resultVO.setPassed(total > 0 && score >= KnowledgeMasteryComponent.REVIEW_CORRECT_PERCENT);
        resultVO.setCorrectCount(correctCount);
        resultVO.setTotalCount(total);
        resultVO.setScore(score);
        resultVO.setMasteryScore(mastery == null || mastery.getMasteryScore() == null ? 0 : mastery.getMasteryScore());
        resultVO.setMastered(mastery != null && mastery.getStatus() != null
                && mastery.getStatus() == KnowledgeMasteryComponent.STATUS_MASTERED);
        resultVO.setResults(results);
        return resultVO;
    }

    // ==================== 详情组装 ====================

    private LearningPathVO buildDetailVO(LearningPath path, List<LearningPathItem> items, Narrative narrative,
                                        Map<String, KnowledgeMastery> masteryMap,
                                        Map<String, KnowledgePoint> pointMap) {
        LearningPathVO vo = new LearningPathVO();
        vo.setPathId(path.getPathId());
        vo.setTitle(path.getTitle());
        vo.setStage(path.getStage());
        vo.setStatus(path.getStatus());
        vo.setTotalItems(path.getTotalItems());
        vo.setFinishedItems(path.getFinishedItems());
        vo.setProgress(path.getProgress());
        vo.setCurrentNodeId(path.getCurrentItemId());
        vo.setCreateTime(formatTime(path.getCreateTime()));
        vo.setUpdateTime(formatTime(path.getUpdateTime()));
        if (narrative != null) {
            vo.setGoal(narrative.goal());
            vo.setOutcome(narrative.outcome());
            vo.setCadence(narrative.cadence());
            vo.setStartHint(narrative.startHint());
        }

        Map<String, LearningPathGenerateComponent.PlanNode> narrativeNodes = indexNarrativeNodes(narrative);
        Date now = new Date();
        String currentItemId = path.getCurrentItemId();
        String currentNodeName = currentNodeName(path, items);

        // 主线按叙事层阶段分组；无叙事层（旧版）则归入单一阶段
        List<LearningPathStageVO> stages = new ArrayList<>();
        Map<String, LearningPathStageVO> stageByName = new LinkedHashMap<>();
        if (narrative != null && !narrative.stages().isEmpty()) {
            for (LearningPathGenerateComponent.StageDraft stage : narrative.stages()) {
                LearningPathStageVO stageVO = new LearningPathStageVO();
                stageVO.setName(stage.name());
                stageVO.setGoal(stage.goal());
                stageVO.setCheckpoint(stage.checkpoint());
                stageVO.setNodes(new ArrayList<>());
                stages.add(stageVO);
                stageByName.put(stage.name(), stageVO);
            }
        }
        LearningPathStageVO fallbackStage = null;

        // 前置节点：主线「学习类」节点按顺序串联，锁定节点给出具体前置节点名
        String previousMainLearnName = null;
        List<LearningPathNodeVO> orderedMain = new ArrayList<>();
        Map<String, List<LearningPathNodeVO>> branchNodes = new LinkedHashMap<>();

        for (LearningPathItem item : items) {
            LearningPathNodeVO node = buildNodeVO(item, masteryMap.get(item.getKnowledgePointId()),
                    pointMap.get(item.getKnowledgePointId()), narrativeNodes.get(item.getKnowledgePointName()), now);
            boolean isBranch = item.getBranchType() != null
                    && item.getBranchType() == LearningPathComponent.BRANCH_TYPE_INTEREST;
            if (isBranch) {
                String branchName = StringTools.isEmpty(item.getBranchName()) ? "兴趣拓展" : item.getBranchName();
                branchNodes.computeIfAbsent(branchName, key -> new ArrayList<>()).add(node);
                continue;
            }
            // 与 common 的解锁链保持一致：主线且非到期复习节点参与顺序解锁
            boolean inChain = item.getBranchType() != null
                    && item.getBranchType() == LearningPathComponent.BRANCH_TYPE_MAIN
                    && item.getDueDate() == null;
            if (inChain && node.getStatus() != null && node.getStatus() == LearningPathComponent.ITEM_STATUS_LOCKED) {
                node.setPrerequisiteName(previousMainLearnName);
            }
            if (inChain) {
                previousMainLearnName = item.getKnowledgePointName();
            }
            orderedMain.add(node);
            LearningPathStageVO stageVO = resolveStage(narrative, stageByName, node.getKnowledgePointName());
            if (stageVO == null) {
                if (fallbackStage == null) {
                    fallbackStage = new LearningPathStageVO();
                    fallbackStage.setName("学习主线");
                    fallbackStage.setNodes(new ArrayList<>());
                    stages.add(fallbackStage);
                }
                stageVO = fallbackStage;
            }
            stageVO.getNodes().add(node);
        }
        for (LearningPathStageVO stage : stages) {
            boolean allMastered = !stage.getNodes().isEmpty();
            for (LearningPathNodeVO node : stage.getNodes()) {
                if (node.getStatus() == null || node.getStatus() != LearningPathComponent.ITEM_STATUS_MASTERED) {
                    allMastered = false;
                    break;
                }
            }
            stage.setFinished(allMastered);
        }
        if (vo.getCurrentNodeId() != null && currentNodeName != null) {
            vo.setCurrentNodeName(currentNodeName);
        }
        vo.setStages(stages);

        List<LearningPathBranchVO> branches = new ArrayList<>();
        for (Map.Entry<String, List<LearningPathNodeVO>> entry : branchNodes.entrySet()) {
            LearningPathBranchVO branch = new LearningPathBranchVO();
            branch.setBranchName(entry.getKey());
            branch.setNodes(entry.getValue());
            branches.add(branch);
        }
        vo.setBranches(branches);
        return vo;
    }

    /**
     * 节点归属阶段：按叙事层阶段内的节点名匹配
     */
    private LearningPathStageVO resolveStage(Narrative narrative, Map<String, LearningPathStageVO> stageByName,
                                             String knowledgePointName) {
        if (narrative == null || knowledgePointName == null) {
            return null;
        }
        for (LearningPathGenerateComponent.StageDraft stage : narrative.stages()) {
            for (LearningPathGenerateComponent.PlanNode node : stage.nodes()) {
                if (knowledgePointName.equals(node.title())) {
                    return stageByName.get(stage.name());
                }
            }
        }
        return null;
    }

    private Map<String, LearningPathGenerateComponent.PlanNode> indexNarrativeNodes(Narrative narrative) {
        Map<String, LearningPathGenerateComponent.PlanNode> map = new HashMap<>();
        if (narrative == null) {
            return map;
        }
        for (LearningPathGenerateComponent.StageDraft stage : narrative.stages()) {
            for (LearningPathGenerateComponent.PlanNode node : stage.nodes()) {
                map.putIfAbsent(node.title(), node);
            }
        }
        for (LearningPathGenerateComponent.BranchDraft branch : narrative.branches()) {
            for (LearningPathGenerateComponent.PlanNode node : branch.nodes()) {
                map.putIfAbsent(node.title(), node);
            }
        }
        return map;
    }

    private LearningPathNodeVO buildNodeVO(LearningPathItem item, KnowledgeMastery mastery, KnowledgePoint point,
                                           LearningPathGenerateComponent.PlanNode narrativeNode, Date now) {
        LearningPathNodeVO node = new LearningPathNodeVO();
        node.setItemId(item.getItemId());
        node.setKnowledgePointId(item.getKnowledgePointId());
        node.setKnowledgePointName(item.getKnowledgePointName());
        node.setBranchType(item.getBranchType());
        node.setBranchName(item.getBranchName());
        node.setItemType(item.getItemType());
        node.setStatus(item.getStatus());
        node.setSort(item.getSort());
        node.setDueDate(formatDate(item.getDueDate()));
        node.setFinishTime(formatTime(item.getFinishTime()));
        if (mastery != null) {
            node.setMasteryScore(mastery.getMasteryScore());
            node.setPracticeCount(mastery.getPracticeCount());
            node.setNextReviewTime(formatTime(mastery.getNextReviewTime()));
            node.setDue(mastery.getNextReviewTime() != null && !mastery.getNextReviewTime().after(now));
        } else {
            node.setMasteryScore(0);
            node.setPracticeCount(0);
            node.setDue(false);
        }
        if (point != null && !StringTools.isEmpty(point.getDescription())) {
            node.setLearningTip(point.getDescription());
        }
        if (narrativeNode != null) {
            node.setTask(narrativeNode.task());
            node.setWay(narrativeNode.way());
            node.setMinutes(narrativeNode.minutes());
            node.setMust(narrativeNode.must());
        }
        return node;
    }

    private String currentNodeName(LearningPath path, List<LearningPathItem> items) {
        if (path.getCurrentItemId() == null) {
            return null;
        }
        for (LearningPathItem item : items) {
            if (path.getCurrentItemId().equals(item.getItemId())) {
                return item.getKnowledgePointName();
            }
        }
        return null;
    }

    // ==================== 叙事层读写（方案 A） ====================

    /** 叙事层（解析自 ai_generation_record.content） */
    private record Narrative(String pathId, String title, String goal, String outcome, String cadence, String startHint,
                             List<LearningPathGenerateComponent.StageDraft> stages,
                             List<LearningPathGenerateComponent.BranchDraft> branches) {
    }

    private void saveNarrative(String userId, String stage, LearningPathGenerateComponent.LearningPathPlan plan,
                               String pathId) {
        try {
            Date now = new Date();
            AiGenerationRecord record = new AiGenerationRecord();
            record.setRecordId(UUID.randomUUID().toString().replace("-", ""));
            record.setUserId(userId);
            record.setStage(stage);
            record.setType(RECORD_TYPE);
            record.setTitle(plan.title());
            record.setContent(plan.toJson(pathId));
            record.setSource(0);
            record.setStatus(1);
            record.setSaved(0);
            record.setAuditStatus(0);
            record.setCreateTime(now);
            record.setUpdateTime(now);
            aiGenerationRecordService.add(record);
        } catch (Exception e) {
            // 叙事层写失败不影响结构层（页面会按旧版样式展示节点）
            log.warn("学习路径叙事层写入失败 userId={} pathId={}", userId, pathId, e);
        }
    }

    private Map<String, Narrative> loadNarrativeMap(String userId) {
        Map<String, Narrative> map = new HashMap<>();
        for (AiGenerationRecord record : listRecords(userId)) {
            Narrative narrative = parseNarrative(record.getContent());
            if (narrative != null && !StringTools.isEmpty(narrative.pathId())) {
                map.putIfAbsent(narrative.pathId(), narrative);
            }
        }
        return map;
    }

    private Narrative loadNarrative(String userId, String pathId) {
        return loadNarrativeMap(userId).get(pathId);
    }

    private List<AiGenerationRecord> listRecords(String userId) {
        AiGenerationRecordQuery query = new AiGenerationRecordQuery();
        query.setUserId(userId);
        query.setType(RECORD_TYPE);
        query.setOrderBy("create_time desc");
        List<AiGenerationRecord> records = aiGenerationRecordService.findListByParam(query);
        return records == null ? new ArrayList<>() : records;
    }

    private Narrative parseNarrative(String content) {
        if (StringTools.isEmpty(content)) {
            return null;
        }
        try {
            JSONObject root = JSON.parseObject(content);
            if (root == null) {
                return null;
            }
            List<LearningPathGenerateComponent.StageDraft> stages = new ArrayList<>();
            JSONArray stageArray = root.getJSONArray("stages");
            if (stageArray != null) {
                for (int i = 0; i < stageArray.size(); i++) {
                    JSONObject stage = stageArray.getJSONObject(i);
                    if (stage == null) {
                        continue;
                    }
                    stages.add(new LearningPathGenerateComponent.StageDraft(
                            stage.getString("name"),
                            stage.getString("goal"),
                            stage.getString("checkpoint"),
                            parseNarrativeNodes(stage.getJSONArray("nodes"))));
                }
            }
            List<LearningPathGenerateComponent.BranchDraft> branches = new ArrayList<>();
            JSONArray branchArray = root.getJSONArray("branches");
            if (branchArray != null) {
                for (int i = 0; i < branchArray.size(); i++) {
                    JSONObject branch = branchArray.getJSONObject(i);
                    if (branch == null) {
                        continue;
                    }
                    branches.add(new LearningPathGenerateComponent.BranchDraft(
                            branch.getString("name"), parseNarrativeNodes(branch.getJSONArray("nodes"))));
                }
            }
            return new Narrative(root.getString("pathId"), root.getString("title"),
                    root.getString("goal"), root.getString("outcome"), root.getString("cadence"),
                    root.getString("startHint"), stages, branches);
        } catch (Exception e) {
            log.warn("学习路径叙事层解析失败（按旧版处理）", e);
            return null;
        }
    }

    private List<LearningPathGenerateComponent.PlanNode> parseNarrativeNodes(JSONArray array) {
        List<LearningPathGenerateComponent.PlanNode> nodes = new ArrayList<>();
        if (array == null) {
            return nodes;
        }
        for (int i = 0; i < array.size(); i++) {
            JSONObject node = array.getJSONObject(i);
            if (node == null) {
                continue;
            }
            nodes.add(new LearningPathGenerateComponent.PlanNode(
                    node.getString("title"),
                    node.getString("task"),
                    node.getString("way"),
                    node.getInteger("minutes"),
                    node.getBoolean("must"),
                    node.getString("kind")));
        }
        return nodes;
    }

    // ==================== 其他 ====================

    private Map<String, KnowledgeMastery> loadMasteryMap(String userId) {
        Map<String, KnowledgeMastery> map = new HashMap<>();
        KnowledgeMasteryQuery query = new KnowledgeMasteryQuery();
        query.setUserId(userId);
        List<KnowledgeMastery> list = knowledgeMasteryService.findListByParam(query);
        if (list != null) {
            for (KnowledgeMastery mastery : list) {
                map.put(mastery.getKnowledgePointId(), mastery);
            }
        }
        return map;
    }

    private Map<String, KnowledgePoint> loadPointMap() {
        Map<String, KnowledgePoint> map = new HashMap<>();
        try {
            List<KnowledgePoint> list = knowledgePointService.findListByParam(new KnowledgePointQuery());
            if (list != null) {
                for (KnowledgePoint point : list) {
                    map.put(point.getKnowledgePointId(), point);
                }
            }
        } catch (Exception e) {
            log.warn("读取知识点失败（学习建议将缺失）", e);
        }
        return map;
    }

    private String buildProfileText(UserWikiProfile profile) {
        StringBuilder builder = new StringBuilder();
        if (!StringTools.isEmpty(profile.getLearningGoal())) {
            builder.append("学习目标：").append(profile.getLearningGoal()).append("\n");
        }
        if (!StringTools.isEmpty(profile.getInterestSubjects())) {
            builder.append("感兴趣学科/主题：").append(profile.getInterestSubjects()).append("\n");
        }
        if (!StringTools.isEmpty(profile.getKeyQuestions())) {
            builder.append("关键问题：").append(profile.getKeyQuestions()).append("\n");
        }
        if (!StringTools.isEmpty(profile.getAliasTerms())) {
            builder.append("我的术语叫法：").append(profile.getAliasTerms()).append("\n");
        }
        return builder.toString().trim();
    }

    private List<String> listLearnedTitles(String userId) {
        KnowledgeDocQuery query = new KnowledgeDocQuery();
        query.setOwnerId(userId);
        query.setVectorStatus(VECTOR_STATUS_CONFIRMED);
        query.setOrderBy("update_time desc");
        List<KnowledgeDoc> docs = knowledgeDocService.findListByParam(query);
        return docs.stream().map(KnowledgeDoc::getTitle).filter(title -> !StringTools.isEmpty(title)).toList();
    }

    private String formatTime(Date date) {
        return date == null ? null : DateUtil.format(date, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern());
    }

    private String formatDate(Date date) {
        return date == null ? null : DateUtil.format(date, DateTimePatternEnum.YYYY_MM_DD.getPattern());
    }
}
