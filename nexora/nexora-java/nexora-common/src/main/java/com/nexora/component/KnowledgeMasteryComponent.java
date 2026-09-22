package com.nexora.component;

import com.nexora.entity.po.KnowledgeMastery;
import com.nexora.entity.query.KnowledgeMasteryQuery;
import com.nexora.service.KnowledgeMasteryService;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识点掌握度回写组件（knowledge_mastery 的唯一写入方）。
 *
 * 课时通关测验判分、主观题人工批阅都经此回写，供学习分析、学生端「学习进度」与个性化学习路径（遗忘曲线复习）消费。
 *
 * 口径（调整口径只需改本类常量与 applyAnswers 的分支）：
 * 1. 掌握度分数 = 历史正确率 = 答对次数 ÷ 练习次数 × 100（四舍五入）；
 * 2. 「已掌握」判定 = 练习次数 ≥ 3 且 分数 ≥ 80，否则「进行中」；
 * 3. 刚跨入已掌握：记掌握时间、复习阶段归 0、下次复习 = 当前 + 1 天；
 * 4. 已掌握后本批全部答对（视为一次复习通过）：复习阶段前进一档（上限 4），下次复习 = 当前 + 1/3/7/15/30 天；
 * 5. 已掌握后掉出掌握线（答错把分数拉低）：回炉「进行中」，复习阶段归 0，下次复习 = 当前时间（立即进入待复习）；
 * 6. 从未掌握：不排复习（下次复习为空，属于「学习中」而非「待复习」）。
 *
 * 写库策略：按 uk_user_kp（user_id + knowledge_point_id）做批量 upsert——一次查询 + 一次批量写入，
 * 不在循环里逐条查库；同一批内同一知识点的多次作答会先合并再写。
 */
@Slf4j
@Component
public class KnowledgeMasteryComponent {

    /** 判定「已掌握」所需的最小练习次数 */
    public static final int MASTER_MIN_PRACTICE = 3;

    /** 判定「已掌握」的掌握度分数线（历史正确率，百分制） */
    public static final int MASTER_SCORE_THRESHOLD = 80;

    /** 批阅得分率达到该比例视为答对（用于主观题人工批阅回写） */
    public static final int REVIEW_CORRECT_PERCENT = 80;

    /** 遗忘曲线复习间隔（天），索引即复习阶段 0-4 */
    public static final int[] REVIEW_INTERVALS_DAYS = {1, 3, 7, 15, 30};

    /** 掌握状态：进行中 */
    public static final int STATUS_LEARNING = 1;

    /** 掌握状态：已掌握 */
    public static final int STATUS_MASTERED = 2;

    @Resource
    private KnowledgeMasteryService knowledgeMasteryService;

    /**
     * 一次作答的结果（客观题自动判分 / 主观题批阅后）
     *
     * @param knowledgePointId 知识点ID（为空表示题目未挂知识点，不参与掌握度）
     * @param correct          本次是否答对
     */
    public record AnswerOutcome(String knowledgePointId, boolean correct) {
    }

    /**
     * 批量回写掌握度：单次查询 + 单次批量 upsert；回写失败只记日志，不影响判分主流程
     */
    public void recordAnswers(String userId, String stage, List<AnswerOutcome> outcomes) {
        if (StringTools.isEmpty(userId) || outcomes == null || outcomes.isEmpty()) {
            return;
        }
        Map<String, Integer> attemptMap = new LinkedHashMap<>();
        Map<String, Integer> correctMap = new LinkedHashMap<>();
        for (AnswerOutcome outcome : outcomes) {
            if (outcome == null || StringTools.isEmpty(outcome.knowledgePointId())) {
                continue;
            }
            String knowledgePointId = outcome.knowledgePointId().trim();
            attemptMap.merge(knowledgePointId, 1, Integer::sum);
            if (outcome.correct()) {
                correctMap.merge(knowledgePointId, 1, Integer::sum);
            }
        }
        if (attemptMap.isEmpty()) {
            return;
        }
        Map<String, KnowledgeMastery> existing = loadExisting(userId, attemptMap.keySet());
        Date now = new Date();
        List<KnowledgeMastery> upsertList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : attemptMap.entrySet()) {
            String knowledgePointId = entry.getKey();
            int attempts = entry.getValue();
            int corrects = correctMap.getOrDefault(knowledgePointId, 0);
            upsertList.add(buildUpsert(userId, stage, knowledgePointId, attempts, corrects,
                    existing.get(knowledgePointId), now));
        }
        try {
            knowledgeMasteryService.addOrUpdateBatch(upsertList);
        } catch (Exception e) {
            log.warn("掌握度批量回写失败 userId={} 知识点数={}", userId, upsertList.size(), e);
        }
    }

    /**
     * 单条作答回写（主观题批阅等单条场景）
     */
    public void recordAnswer(String userId, String stage, String knowledgePointId, boolean correct) {
        recordAnswers(userId, stage, List.of(new AnswerOutcome(knowledgePointId, correct)));
    }

    /**
     * 一次查出该生已有掌握度（按 uk_user_kp 建索引），避免循环查库
     */
    private Map<String, KnowledgeMastery> loadExisting(String userId, java.util.Set<String> knowledgePointIds) {
        Map<String, KnowledgeMastery> result = new HashMap<>();
        try {
            KnowledgeMasteryQuery query = new KnowledgeMasteryQuery();
            query.setUserId(userId);
            List<KnowledgeMastery> list = knowledgeMasteryService.findListByParam(query);
            if (list != null) {
                for (KnowledgeMastery item : list) {
                    if (item.getKnowledgePointId() != null && knowledgePointIds.contains(item.getKnowledgePointId())) {
                        result.put(item.getKnowledgePointId(), item);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("掌握度查询失败，按首次作答处理 userId={}", userId, e);
        }
        return result;
    }

    /**
     * 计算并组装一条 upsert 记录（全字段覆盖，未掌握时复习字段显式置空）
     */
    private KnowledgeMastery buildUpsert(String userId, String stage, String knowledgePointId, int attempts,
                                        int corrects, KnowledgeMastery existing, Date now) {
        int prevPractice = existing == null || existing.getPracticeCount() == null ? 0 : existing.getPracticeCount();
        int prevCorrect = existing == null || existing.getCorrectCount() == null ? 0 : existing.getCorrectCount();
        int prevStatus = existing == null || existing.getStatus() == null ? STATUS_LEARNING : existing.getStatus();
        boolean wasMastered = prevStatus == STATUS_MASTERED;
        Integer prevStage = existing == null ? null : existing.getReviewStage();

        int practiceCount = prevPractice + attempts;
        int correctCount = prevCorrect + corrects;
        int masteryScore = (int) Math.round(correctCount * 100.0 / practiceCount);
        boolean mastered = practiceCount >= MASTER_MIN_PRACTICE && masteryScore >= MASTER_SCORE_THRESHOLD;

        KnowledgeMastery bean = new KnowledgeMastery();
        bean.setUserId(userId);
        bean.setKnowledgePointId(knowledgePointId);
        bean.setStage(stage);
        bean.setMasteryScore(masteryScore);
        bean.setPracticeCount(practiceCount);
        bean.setCorrectCount(correctCount);
        bean.setLastPracticeTime(now);
        bean.setUpdateTime(now);
        bean.setCreateTime(existing == null || existing.getCreateTime() == null ? now : existing.getCreateTime());

        if (mastered) {
            bean.setStatus(STATUS_MASTERED);
            if (!wasMastered) {
                // 刚跨入已掌握：遗忘曲线从今天起步
                bean.setLastMasterTime(now);
                bean.setReviewStage(0);
                bean.setNextReviewTime(plusDays(now, REVIEW_INTERVALS_DAYS[0]));
            } else {
                bean.setLastMasterTime(existing.getLastMasterTime() == null ? now : existing.getLastMasterTime());
                if (corrects >= attempts) {
                    // 复习通过：阶段前进一档，下一次复习间隔拉长
                    int nextStage = Math.min((prevStage == null ? 0 : prevStage) + 1, REVIEW_INTERVALS_DAYS.length - 1);
                    bean.setReviewStage(nextStage);
                    bean.setNextReviewTime(plusDays(now, REVIEW_INTERVALS_DAYS[nextStage]));
                } else {
                    // 本批有错但仍在掌握线之上：保持原复习计划
                    bean.setReviewStage(prevStage == null ? 0 : prevStage);
                    bean.setNextReviewTime(existing.getNextReviewTime());
                }
            }
        } else {
            bean.setStatus(STATUS_LEARNING);
            if (wasMastered) {
                // 掉出掌握线 → 回炉：立即进入待复习
                bean.setReviewStage(0);
                bean.setNextReviewTime(now);
            } else {
                // 学习中：不排复习
                bean.setReviewStage(0);
                bean.setNextReviewTime(null);
            }
            bean.setLastMasterTime(wasMastered && existing != null ? existing.getLastMasterTime() : null);
        }
        return bean;
    }

    private Date plusDays(Date base, int days) {
        return Date.from(Instant.ofEpochMilli(base.getTime()).plus(days, ChronoUnit.DAYS));
    }
}
