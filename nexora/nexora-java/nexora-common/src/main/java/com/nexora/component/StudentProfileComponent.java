package com.nexora.component;

import com.nexora.entity.po.LearningPath;
import com.nexora.entity.po.LearningPathItem;
import com.nexora.entity.po.StudentPointAccount;
import com.nexora.entity.po.StudentProfile;
import com.nexora.entity.query.LearningPathItemQuery;
import com.nexora.entity.query.LearningPathQuery;
import com.nexora.entity.vo.KnowledgeMasteryVO;
import com.nexora.mappers.LearningAnalysisMapper;
import com.nexora.mappers.StudentPointAccountMapper;
import com.nexora.mappers.StudentProfileMapper;
import com.nexora.service.LearningPathItemService;
import com.nexora.service.LearningPathService;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学生画像（计划 C1）：把散落在掌握度 / 学习路径 / 积分三处的学情，聚合成一份**速览文本**注入对话提示词。
 *
 * 设计取舍（详见 docs/二期规划设计-20261006.md §5.2.3）：
 * 1. 画像只给"概览"（约 120 字），细节一律回源查询（queryMastery / queryPathNode / planNextStep），避免提示词膨胀；
 * 2. 重建由学习事件驱动：掌握度、积分、路径状态变化时只**打标记**（markDirty），下次对话前重建一次，
 *    避免每次事件都做聚合查询；另外 24 小时兜底重建，防止标记丢失导致画像长期陈旧；
 * 3. 画像重建失败绝不影响对话：调用方（AgentChatComponent）拿到空串就不注入，异常在这里吞掉并记日志。
 */
@Slf4j
@Component
public class StudentProfileComponent {

    /** 画像最长有效期：超过则即使没有 dirty 也重建一次 */
    private static final Duration MAX_AGE = Duration.ofHours(24);

    /** 薄弱知识点判定：练习过且掌握度低于该值 */
    private static final int WEAK_SCORE = 70;

    /** 薄弱点/最弱知识点最多列几个 */
    private static final int WEAK_LIMIT = 5;

    private static final int WEAKEST_LIMIT = 3;

    /** 高于该分数的不算「最弱」（画像里避免出现"最弱: xxx(100)"这类自相矛盾的表述） */
    private static final int WELL_DONE_SCORE = 85;

    @Resource
    private StudentProfileMapper profileMapper;

    @Resource
    private LearningAnalysisMapper learningAnalysisMapper;

    @Resource
    private StudentPointAccountMapper accountMapper;

    @Resource
    private PointLevelComponent levelComponent;

    @Resource
    private LearningPathService learningPathService;

    @Resource
    private LearningPathItemService learningPathItemService;

    /**
     * 标记待刷新（学习事件调用：掌握度写入、积分发放、路径状态刷新）。
     * 只打标记不重建，成本是一次小更新；异常只记日志，绝不影响学习链路。
     */
    public void markDirty(String userId) {
        if (StringTools.isEmpty(userId)) {
            return;
        }
        try {
            profileMapper.markDirty(userId);
        } catch (Exception e) {
            log.warn("画像标记待刷新失败（不影响学习链路）userId={}", userId, e);
        }
    }

    /**
     * 取"画像速览"一行文本（供系统提示词注入）。需要时先重建；失败或无事可写时返回空串。
     */
    public String profileLine(String userId, String stage, String grade) {
        if (StringTools.isEmpty(userId)) {
            return "";
        }
        try {
            StudentProfile profile = profileMapper.selectByUserId(userId);
            if (needRebuild(profile)) {
                profile = rebuild(userId, stage, grade);
            }
            return compose(profile);
        } catch (Exception e) {
            log.warn("学生画像获取失败（本轮不注入画像）userId={}", userId, e);
            return "";
        }
    }

    /** 画像锚点：《我的学习偏好》知识页 ID（计划 C3 用；未建页时为空） */
    public String preferenceDocId(String userId) {
        try {
            StudentProfile profile = profileMapper.selectByUserId(userId);
            return profile == null ? null : profile.getPreferenceDocId();
        } catch (Exception e) {
            log.warn("读取偏好页锚点失败 userId={}", userId, e);
            return null;
        }
    }

    /** 绑定《我的学习偏好》知识页 ID（计划 C3 创建该页后调用） */
    public void bindPreferenceDoc(String userId, String docId) {
        if (StringTools.isEmpty(userId) || StringTools.isEmpty(docId)) {
            return;
        }
        try {
            profileMapper.updatePreferenceDocId(userId, docId);
        } catch (Exception e) {
            log.warn("绑定偏好页锚点失败 userId={} docId={}", userId, docId, e);
        }
    }

    private boolean needRebuild(StudentProfile profile) {
        if (profile == null) {
            return true;
        }
        if (profile.getDirty() != null && profile.getDirty() == 1) {
            return true;
        }
        Date refresh = profile.getRefreshTime();
        if (refresh == null) {
            return true;
        }
        return refresh.toInstant().plus(MAX_AGE).isBefore(Instant.now());
    }

    /** 重建画像：聚合成三段摘要后覆盖写回（聚合查询各一次，不在循环里查库） */
    private StudentProfile rebuild(String userId, String stage, String grade) {
        String masterySummary = buildMasterySummary(userId);
        String weakPoints = buildWeakPoints(userId);
        String pathSummary = buildPathSummary(userId);
        String pointSummary = buildPointSummary(userId, stage);

        StudentProfile profile = new StudentProfile();
        profile.setUserId(userId);
        profile.setStage(stage);
        profile.setGrade(grade);
        profile.setMasterySummary(masterySummary);
        profile.setWeakPoints(weakPoints);
        profile.setPathSummary(pathSummary);
        profile.setPointSummary(pointSummary);
        profileMapper.upsertSummary(profile);
        profile.setDirty(0);
        profile.setRefreshTime(new Date());
        return profile;
    }

    /** 掌握度摘要：数量分布 + 平均分 + 最弱 3 个知识点 */
    private String buildMasterySummary(String userId) {
        List<KnowledgeMasteryVO> list = learningAnalysisMapper.selectMasteryList(userId);
        if (list == null || list.isEmpty()) {
            return "暂无掌握度数据（还没做过练习）";
        }
        int mastered = 0;
        int inProgress = 0;
        int locked = 0;
        int totalScore = 0;
        for (KnowledgeMasteryVO item : list) {
            int status = item.getStatus() == null ? 0 : item.getStatus();
            if (status == 2) {
                mastered++;
            } else if (status == 1) {
                inProgress++;
            } else {
                locked++;
            }
            totalScore += item.getMasteryScore() == null ? 0 : item.getMasteryScore();
        }
        // 「最弱」只列真正偏低的：练习过且分数低于 WELL_DONE_SCORE，避免出现「最弱: xxx(100)」这种自相矛盾的表述
        List<KnowledgeMasteryVO> weakest = list.stream()
                .filter(item -> item.getPracticeCount() != null && item.getPracticeCount() > 0)
                .filter(item -> (item.getMasteryScore() == null ? 0 : item.getMasteryScore()) < WELL_DONE_SCORE)
                .sorted(Comparator.comparingInt(item -> item.getMasteryScore() == null ? 0 : item.getMasteryScore()))
                .limit(WEAKEST_LIMIT)
                .toList();
        String weakestText = weakest.stream()
                .map(item -> item.getKnowledgePointName() + "(" + (item.getMasteryScore() == null ? 0 : item.getMasteryScore()) + ")")
                .collect(Collectors.joining("、"));
        StringBuilder sb = new StringBuilder();
        sb.append("掌握度：共 ").append(list.size()).append(" 个知识点，已掌握 ").append(mastered)
                .append("、进行中 ").append(inProgress).append("、未解锁 ").append(locked)
                .append("，平均分 ").append(totalScore / list.size());
        if (!weakestText.isBlank()) {
            sb.append("；最弱：").append(weakestText);
        }
        return sb.toString();
    }

    /** 薄弱知识点清单：练习过且掌握度 < 70，按分数升序最多 5 条 */
    private String buildWeakPoints(String userId) {
        List<KnowledgeMasteryVO> list = learningAnalysisMapper.selectMasteryList(userId);
        if (list == null || list.isEmpty()) {
            return "";
        }
        List<String> weak = new ArrayList<>();
        for (KnowledgeMasteryVO item : list) {
            int score = item.getMasteryScore() == null ? 0 : item.getMasteryScore();
            int practice = item.getPracticeCount() == null ? 0 : item.getPracticeCount();
            if (practice > 0 && score < WEAK_SCORE) {
                weak.add(item.getKnowledgePointName() + "(" + score + "，" + practice + "次)");
                if (weak.size() >= WEAK_LIMIT) {
                    break;
                }
            }
        }
        return String.join("、", weak);
    }

    /** 路径摘要：进行中路径数 + 当前路径与当前节点 + 进度 */
    private String buildPathSummary(String userId) {
        LearningPathQuery pathQuery = new LearningPathQuery();
        pathQuery.setUserId(userId);
        List<LearningPath> paths = learningPathService.findListByParam(pathQuery);
        if (paths == null || paths.isEmpty()) {
            return "还没有学习路径";
        }
        List<LearningPath> running = new ArrayList<>();
        for (LearningPath path : paths) {
            if (path.getStatus() == null || path.getStatus() != 1) {
                running.add(path);
            }
        }
        if (running.isEmpty()) {
            return "已走完全部 " + paths.size() + " 条学习路径";
        }
        // 当前路径取最近更新的那条；当前节点按其 currentItemId 定位（一次查全部节点，不在循环里查库）
        LearningPath current = running.get(0);
        LearningPathItemQuery itemQuery = new LearningPathItemQuery();
        itemQuery.setUserId(userId);
        List<LearningPathItem> items = learningPathItemService.findListByParam(itemQuery);
        Map<String, String> nameByItemId = (items == null ? List.<LearningPathItem>of() : items).stream()
                .filter(item -> item.getItemId() != null)
                .collect(Collectors.toMap(LearningPathItem::getItemId,
                        item -> item.getKnowledgePointName() == null ? "未知节点" : item.getKnowledgePointName(),
                        (first, second) -> first));
        String currentNode = current.getCurrentItemId() == null ? null : nameByItemId.get(current.getCurrentItemId());
        StringBuilder sb = new StringBuilder();
        sb.append("学习路径：共 ").append(paths.size()).append(" 条，进行中 ").append(running.size())
                .append(" 条；当前在《").append(current.getTitle()).append("》");
        if (!StringTools.isEmpty(currentNode)) {
            sb.append("的节点《").append(currentNode).append("》");
        }
        sb.append("，进度 ").append(current.getProgress() == null ? 0 : current.getProgress()).append("%");
        return sb.toString();
    }

    /** 积分摘要：累计/可用/连续天数/等级称谓（与学生端展示同一口径） */
    private String buildPointSummary(String userId, String stage) {
        StudentPointAccount account = accountMapper.selectByUserId(userId);
        if (account == null) {
            return "积分：还没有获得积分";
        }
        int total = account.getTotalPoints() == null ? 0 : account.getTotalPoints();
        int level = levelComponent.levelOf(total);
        String levelName = levelComponent.levelName(stage == null ? account.getStage() : stage, level);
        StringBuilder sb = new StringBuilder();
        sb.append("积分：累计 ").append(total)
                .append("、可用 ").append(account.getAvailablePoints() == null ? 0 : account.getAvailablePoints())
                .append("、连续学习 ").append(account.getStreakDays() == null ? 0 : account.getStreakDays()).append(" 天");
        if (!StringTools.isEmpty(levelName)) {
            sb.append("、段位 ").append(levelName).append(" ").append(level).append(" 段");
        } else {
            sb.append("、等级 ").append(level).append(" 颗星");
        }
        return sb.toString();
    }

    /** 组装注入用的一行速览（空段自动跳过，控制在约 150 字内） */
    private String compose(StudentProfile profile) {
        if (profile == null) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        if (!StringTools.isEmpty(profile.getPointSummary())) {
            parts.add(profile.getPointSummary());
        }
        if (!StringTools.isEmpty(profile.getMasterySummary())) {
            parts.add(profile.getMasterySummary());
        }
        if (!StringTools.isEmpty(profile.getWeakPoints())) {
            parts.add("待补强：" + profile.getWeakPoints());
        }
        if (!StringTools.isEmpty(profile.getPathSummary())) {
            parts.add(profile.getPathSummary());
        }
        if (parts.isEmpty()) {
            return "";
        }
        return String.join("｜", parts);
    }
}
