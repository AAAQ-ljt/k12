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

    /** 复习通过线（正确率百分比）：达到即视为复习通过、间隔拉长；未达到则回落 1 天 */
    private static final int PASS_RATE_PERCENT = 60;

    /** 掌握状态：进行中 */
    public static final int STATUS_LEARNING = 1;

    /** 掌握状态：已掌握 */
    public static final int STATUS_MASTERED = 2;

    @Resource
    private KnowledgeMasteryService knowledgeMasteryService;

    /** 积分发放入口（跨入「已掌握」时发分；唯一写入方，见二期规划 A-2 第 5 条） */
    @Resource
    private PointAwardComponent pointAwardComponent;

    /**
     * 一次作答的结果（客观题自动判分 / 主观题批阅后）
     *
     * @param knowledgePointId 知识点ID（为空表示题目未挂知识点，不参与掌握度）
     * @param correct          本次是否答对
     */
    public record AnswerOutcome(String knowledgePointId, boolean correct) {
    }

    /**
     * 批量回写掌握度：单次查询 + 单次批量 upsert。
     *
     * <p>回写失败不影响判分主流程（判分照常返回），但**必须把失败如实告诉调用方**：
     * 调用方据此提示学生「成绩已判、掌握度未计入」，否则前端会谎称「下次复习时间已后推」，
     * 而学习进度 / 今日待办仍显示「该复习了」（2026-10-07 实际踩到：进程未重启导致 upsert 报错，
     * 学生做完复习题看到「通过」，进度却一动不动）。
     *
     * @return true = 已成功写入（或本就无需写入）；false = 写入异常，本次成绩未计入掌握度
     */
    public boolean recordAnswers(String userId, String stage, List<AnswerOutcome> outcomes) {
        if (StringTools.isEmpty(userId) || outcomes == null || outcomes.isEmpty()) {
            return true;
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
            return true;
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
            log.error("掌握度批量回写失败 userId={} 知识点数={}（本次成绩未计入掌握度，需提示学生）",
                    userId, upsertList.size(), e);
            return false;
        }
        // 跨入「已掌握」→ 发积分（二期积分体系 A-2 第 5 条）。
        // 幂等由 PointAwardComponent 保证（bizId = 知识点 ID，同一知识点只奖一次）；
        // 掌握度写失败时不发分（上面已 return），避免「分数发了但掌握度没落库」。
        // 另外：积分属于激励层，任何发分异常都只记日志、不向上抛 —— 绝不能因为积分问题让学生答不了题
        // （最典型场景：积分表尚未建好就部署了新 jar）。
        for (KnowledgeMastery bean : upsertList) {
            if (bean.getStatus() == null || bean.getStatus() != STATUS_MASTERED) {
                continue;
            }
            KnowledgeMastery before = existing.get(bean.getKnowledgePointId());
            if (before != null && before.getStatus() != null && before.getStatus() == STATUS_MASTERED) {
                continue;
            }
            try {
                int gained = pointAwardComponent.awardMastery(userId, stage, bean.getKnowledgePointId());
                if (gained > 0) {
                    log.info("知识点已掌握并发积分 userId={} knowledgePointId={} points={}",
                            userId, bean.getKnowledgePointId(), gained);
                }
            } catch (Exception e) {
                log.warn("知识点掌握积分发放失败（不影响学习链路）userId={} knowledgePointId={}",
                        userId, bean.getKnowledgePointId(), e);
            }
        }
        return true;
    }

    /**
     * 单条作答回写（主观题批阅等单条场景）
     */
    public void recordAnswer(String userId, String stage, String knowledgePointId, boolean correct) {
        recordAnswers(userId, stage, List.of(new AnswerOutcome(knowledgePointId, correct)));
    }

    /**
     * 该知识点是否已有掌握度记录（练过 / 掌握过）。
     *
     * <p>用途：一个已学过的节点可能因为前面某个节点回炉掉线而被重新置为「未解锁」，
     * 但它自己的复习计划还在跑（页面「待复习 / 今日待办」会提示该复习了）——
     * 这种情况下必须允许复习快测，否则那个「该复习了」永远消不掉（2026-10-08 修）。
     */
    public boolean hasRecord(String userId, String knowledgePointId) {
        if (StringTools.isEmpty(userId) || StringTools.isEmpty(knowledgePointId)) {
            return false;
        }
        try {
            KnowledgeMasteryQuery query = new KnowledgeMasteryQuery();
            query.setUserId(userId);
            query.setKnowledgePointId(knowledgePointId);
            List<KnowledgeMastery> list = knowledgeMasteryService.findListByParam(query);
            return list != null && !list.isEmpty();
        } catch (Exception e) {
            log.warn("掌握度查询失败（按未练过处理）userId={} knowledgePointId={}", userId, knowledgePointId, e);
            return false;
        }
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
                // 复习通过线：正确率 ≥ 60%（原来要求"整批全对"，学生错一题就一直挂着过期时间 → 待复习越堆越多）
                int passLine = Math.max(1, (attempts * PASS_RATE_PERCENT + 99) / 100);
                if (corrects >= passLine) {
                    // 复习通过：阶段前进一档，下一次复习间隔拉长（1→3→7→15→30 天）
                    int nextStage = Math.min((prevStage == null ? 0 : prevStage) + 1, REVIEW_INTERVALS_DAYS.length - 1);
                    bean.setReviewStage(nextStage);
                    bean.setNextReviewTime(plusDays(now, REVIEW_INTERVALS_DAYS[nextStage]));
                } else {
                    // 复习未达标：间隔回落到 1 天（明天再来一次），而不是继续挂着已过期的旧时间
                    bean.setReviewStage(0);
                    bean.setNextReviewTime(plusDays(now, REVIEW_INTERVALS_DAYS[0]));
                }
            }
        } else {
            bean.setStatus(STATUS_LEARNING);
            // 是否曾经掌握过 / 排过复习（回炉后可能已被降级，光看当前 status 判不出来）
            boolean everScheduled = existing != null
                    && (existing.getNextReviewTime() != null || existing.getLastMasterTime() != null);
            if (wasMastered) {
                // 掉出掌握线 → 回炉：立即进入待复习
                bean.setReviewStage(0);
                bean.setNextReviewTime(now);
            } else if (everScheduled) {
                // 回炉后再失手：仍然排明天复习 —— 之前这里清空 next_review_time，
                // 而全仓没有任何地方会重新排期，该知识点会静默从「待复习」里消失，
                // 与前端「明天会再提醒你复习一次」的文案矛盾（2026-10-08 修）
                bean.setReviewStage(0);
                bean.setNextReviewTime(plusDays(now, REVIEW_INTERVALS_DAYS[0]));
            } else {
                // 从没掌握过（正常学习中）：不排复习
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
