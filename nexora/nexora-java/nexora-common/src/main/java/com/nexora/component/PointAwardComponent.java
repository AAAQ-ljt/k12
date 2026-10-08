package com.nexora.component;

import com.nexora.entity.po.GameBadge;
import com.nexora.entity.po.StudentBadgeRecord;
import com.nexora.entity.po.StudentPointAccount;
import com.nexora.entity.po.StudentPointRecord;
import com.nexora.entity.query.GameBadgeQuery;
import com.nexora.entity.vo.PointBadgeVO;
import com.nexora.exception.BusinessException;
import com.nexora.mappers.GameBadgeMapper;
import com.nexora.mappers.StudentBadgeRecordMapper;
import com.nexora.mappers.StudentPointAccountMapper;
import com.nexora.mappers.StudentPointRecordMapper;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 积分发放入口（**全站唯一写入方**，与 {@link KnowledgeMasteryComponent} 同一职责模式）。
 *
 * 职责：幂等发分 → 账户累加 → 等级折算 → 落流水 → 徽章判定（含徽章奖励分）→ 连续学习阶梯。
 * 触发点只负责「把事件告诉它」，不直接碰积分表。
 *
 * 三条不可动摇的口径（见 docs/二期规划设计-20261006.md §3）：
 * 1. **幂等**：唯一索引 `uk_user_biz(user_id, biz_type, biz_id)` 是兜底，方法内先做一次快速判重；
 *    一次性事件用业务主键做 bizId，每日可重复事件用 `yyyyMMdd`（或 `yyyyMMdd:业务ID`）；
 * 2. **每日上限**：只约束「可重复类」来源（签到 / 绘本 / 动画 / 知识页入库等），
 *    一次性事件（课时、题目、知识点）不受限；
 * 3. **编程比赛不进全局积分**：比赛成绩只进比赛自己的排行榜，不在此组件发放范围内（A-2 第 7 条、A-5 分离说明）。
 */
@Slf4j
@Component
public class PointAwardComponent {

    // ====== 积分来源（与 student_point_record.biz_type 注释一致） ======
    public static final String BIZ_SIGN_IN = "SIGN_IN";
    public static final String BIZ_STREAK = "STREAK";
    public static final String BIZ_LESSON_QUIZ = "LESSON_QUIZ";
    /** 学完课时（该课时没有配通关测验，见 CourseStudyBiz 的完成链路） */
    public static final String BIZ_LESSON_DONE = "LESSON_DONE";
    public static final String BIZ_PATH_TEST = "PATH_TEST";
    public static final String BIZ_CODING_PROBLEM = "CODING_PROBLEM";
    public static final String BIZ_PICTURE_BOOK = "PICTURE_BOOK";
    public static final String BIZ_ANIMATION = "ANIMATION";
    public static final String BIZ_WIKI_CONFIRM = "WIKI_CONFIRM";
    public static final String BIZ_MASTERY = "MASTERY";
    public static final String BIZ_BADGE = "BADGE";
    public static final String BIZ_COMBO = "COMBO";

    /** 兑换扣分（A-7）：流水 points 为负，幂等键 = 兑换单号 */
    public static final String BIZ_EXCHANGE = "EXCHANGE";

    /** 学习路径节点完成（幂等键 = 节点 ID） */
    public static final String BIZ_PATH_NODE = "PATH_NODE";

    /** 整条学习路径完成（幂等键 = 路径 ID） */
    public static final String BIZ_PATH_DONE = "PATH_DONE";

    /** 受每日上限约束的「可重复」来源 */
    private static final Set<String> DAILY_CAP_TYPES = new HashSet<>(
            Arrays.asList(BIZ_SIGN_IN, BIZ_PICTURE_BOOK, BIZ_ANIMATION, BIZ_WIKI_CONFIRM));

    /** 「通过类」来源（徽章 FIRST_PASS 判定） */
    private static final List<String> PASS_BIZ_TYPES =
            List.of(BIZ_LESSON_QUIZ, BIZ_LESSON_DONE, BIZ_PATH_TEST, BIZ_CODING_PROBLEM);

    /** 「创作类」来源（徽章 CREATION 判定） */
    private static final List<String> CREATION_BIZ_TYPES =
            List.of(BIZ_PICTURE_BOOK, BIZ_ANIMATION, BIZ_WIKI_CONFIRM);

    /** 连续学习阶梯的天数档位（与 GAME.STREAK_BONUS 的奖励值一一对应） */
    private static final int[] STREAK_TIERS = {3, 7, 15, 30};

    /** 连击档位（连对几题算一档）：达到档位立刻给对应奖励，档内继续连对不重复给 */
    private static final int[] COMBO_TIERS = {3, 5, 10};

    /** 派生连击时取的最近作答条数（够覆盖最高档位即可） */
    private static final int COMBO_SCAN_LIMIT = 60;

    /** 默认连击奖励（对应 3/5/10 连对）；可用 GAME.POINT_COMBO 覆盖 */
    private static final int[] DEFAULT_COMBO_BONUS = {2, 5, 10};

    private static final int DEFAULT_DAILY_CAP = 200;
    private static final int DEFAULT_SIGN_IN_POINTS = 5;
    private static final int DEFAULT_MASTERY_POINTS = 50;
    private static final int DEFAULT_LESSON_QUIZ_POINTS = 20;
    private static final int DEFAULT_LESSON_DONE_POINTS = 10;
    private static final int DEFAULT_PATH_TEST_POINTS = 30;
    private static final int DEFAULT_PICTURE_BOOK_POINTS = 15;
    private static final int DEFAULT_WIKI_CONFIRM_POINTS = 10;
    private static final int DEFAULT_ANIMATION_POINTS = 5;
    private static final int DEFAULT_PATH_NODE_POINTS = 15;
    private static final int DEFAULT_PATH_DONE_POINTS = 50;
    private static final int[] DEFAULT_STREAK_BONUS = {10, 20, 40, 80};

    /** 学生画像（计划 C1：学习事件后标记待刷新） */
    @Resource
    private StudentProfileComponent studentProfileComponent;

    @Resource
    private StudentPointAccountMapper accountMapper;

    @Resource
    private StudentPointRecordMapper recordMapper;

    @Resource
    private GameBadgeMapper badgeMapper;

    @Resource
    private StudentBadgeRecordMapper badgeRecordMapper;

    @Resource
    private SystemConfigComponent systemConfigComponent;

    @Resource
    private PointLevelComponent levelComponent;

    @Resource
    private com.nexora.mappers.PracticeComboMapper practiceComboMapper;

    /** 编程题「看过答案」标记（Redis，TTL 6 小时） */
    @Resource
    private RedisComponent redisComponent;

    /**
     * 发放积分（幂等）。
     *
     * @param bizType 来源（见本类常量；必须与流水表注释中的取值一致）
     * @param bizId   业务幂等键：一次性事件=业务主键；每日可重复事件=`yyyyMMdd` 或 `yyyyMMdd:业务ID`
     * @return 本次实际发放的积分；0 表示「重复事件被拦」或「撞每日上限」
     */
    @Transactional(rollbackFor = Exception.class)
    public int award(String userId, String stage, String bizType, String bizId, int points, String reason) {
        if (StringTools.isEmpty(userId) || StringTools.isEmpty(bizType) || StringTools.isEmpty(bizId) || points == 0) {
            return 0;
        }
        // 1) 快速幂等：同一业务事件只发一次（唯一索引仍是最终兜底）
        if (recordMapper.countByUserAndBiz(userId, bizType, bizId) > 0) {
            return 0;
        }
        // 2) 每日上限：只约束可重复类来源，避免刷分把学习激励变成刷分游戏
        if (points > 0 && DAILY_CAP_TYPES.contains(bizType)) {
            int cap = intConfig("POINT_DAILY_CAP", DEFAULT_DAILY_CAP);
            int todayPoints = recordMapper.sumPointsByUserAndTimeRange(
                    userId, dayStart(), dayEnd(), new ArrayList<>(DAILY_CAP_TYPES));
            if (todayPoints + points > cap) {
                log.info("积分每日上限拦截 userId={} bizType={} 今日已得={} 本次={} 上限={}",
                        userId, bizType, todayPoints, points, cap);
                return 0;
            }
        }
        // 3) 账户累加 + 等级折算
        accountMapper.upsertIncrease(userId, stage, points);
        StudentPointAccount account = accountMapper.selectByUserId(userId);
        if (account == null) {
            log.warn("积分账户异常：加分后读不到账户 userId={}", userId);
            return 0;
        }
        int level = levelComponent.levelOf(account.getTotalPoints() == null ? 0 : account.getTotalPoints());
        if (account.getLevel() == null || account.getLevel() != level) {
            accountMapper.updateLevel(userId, level);
        }
        // 4) 落流水；insertIgnore 返回 0 说明并发下已被写过 → 回滚本次加分
        StudentPointRecord record = new StudentPointRecord();
        record.setUserId(userId);
        record.setStage(stage);
        record.setBizType(bizType);
        record.setBizId(bizId);
        record.setPoints(points);
        record.setBalanceAfter(account.getTotalPoints());
        record.setReason(reason);
        if (recordMapper.insertIgnore(record) == 0) {
            log.info("积分重复发放被幂等拦下 userId={} bizType={} bizId={}", userId, bizType, bizId);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return 0;
        }
        // 5) 徽章判定（可能连带发放徽章奖励分；在同一事务内，自身调用不经过代理仍同事务）
        evaluateBadges(userId, stage, account);
        // 画像待刷新（计划 C1）：发放积分意味着学情变了，下次对话前重建速览
        studentProfileComponent.markDirty(userId);
        return points;
    }

    /**
     * 兑换扣分（二期 A-7）：**只减可用积分，累计积分不动**，并写一条负分流水。
     *
     * 余额校验用条件更新（`where available_points >= cost`）而不是「先查再减」，
     * 并发下也不会把余额扣成负数；余额不足直接抛业务异常，由调用方决定提示文案。
     * 幂等键 = 兑换单号，同一单重复提交会被流水唯一键拦下并回滚本次扣分。
     *
     * @return 扣分后的可用积分
     */
    @Transactional(rollbackFor = Exception.class)
    public int spend(String userId, String stage, String bizId, int cost, String reason) {
        if (StringTools.isEmpty(userId) || StringTools.isEmpty(bizId) || cost <= 0) {
            throw new BusinessException("兑换参数不完整");
        }
        if (accountMapper.spendAvailable(userId, cost) == 0) {
            throw new BusinessException("可用积分不足，本次需要 " + cost + " 分");
        }
        StudentPointAccount account = accountMapper.selectByUserId(userId);
        if (account == null) {
            throw new BusinessException("积分账户异常，请稍后重试");
        }
        StudentPointRecord record = new StudentPointRecord();
        record.setUserId(userId);
        record.setStage(stage);
        record.setBizType(BIZ_EXCHANGE);
        record.setBizId(bizId);
        record.setPoints(-cost);
        // 与其他流水同口径：balance_after 记累计积分；可用积分余额在兑换记录表里
        record.setBalanceAfter(account.getTotalPoints());
        record.setReason(reason);
        if (recordMapper.insertIgnore(record) == 0) {
            log.info("兑换重复提交被幂等拦下 userId={} bizId={}", userId, bizId);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new BusinessException("该兑换单已处理，请刷新后重试");
        }
        // 兑换改了可用积分，画像里的积分摘要也要跟着刷（计划 C1）
        studentProfileComponent.markDirty(userId);
        return account.getAvailablePoints() == null ? 0 : account.getAvailablePoints();
    }

    /**
     * 每日签到（每日首次学习触发）：按日期幂等 + 连续学习阶梯奖励。
     *
     * 调用方在学习链路（课程/资源学习、任意学习行为）里调用即可，重复调用无副作用：
     * 同一天第二次进入时 SIGN_IN 事件被幂等拦下，连续天数保持当天已算出的值。
     *
     * @return 本次实际发放的积分（签到 + 可能命中的连击阶梯）
     */
    @Transactional(rollbackFor = Exception.class)
    public int signIn(String userId, String stage) {
        if (StringTools.isEmpty(userId)) {
            return 0;
        }
        LocalDate today = LocalDate.now();
        String dayKey = today.toString().replace("-", "");
        int gained = award(userId, stage, BIZ_SIGN_IN, dayKey,
                intConfig("POINT_SIGN_IN", DEFAULT_SIGN_IN_POINTS), "每日学习签到");
        // 连续天数：昨天签过 → +1；今天已签过 → 保持；否则断签重置为 1
        StudentPointAccount account = accountMapper.selectByUserId(userId);
        if (account == null) {
            return gained;
        }
        LocalDate last = account.getLastStreakDate() == null
                ? null : account.getLastStreakDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int current = account.getStreakDays() == null ? 0 : account.getStreakDays();
        int streak;
        if (last != null && last.equals(today)) {
            streak = Math.max(current, 1);
        } else if (last != null && last.plusDays(1).equals(today)) {
            streak = current + 1;
        } else {
            streak = 1;
        }
        accountMapper.updateStreak(userId, streak, java.sql.Date.valueOf(today));
        // 阶梯奖励：正好达到档位的那天发一次（bizId 带天数档位，天然幂等）
        int[] bonuses = intArrayConfig("STREAK_BONUS", DEFAULT_STREAK_BONUS);
        for (int i = 0; i < STREAK_TIERS.length && i < bonuses.length; i++) {
            if (streak == STREAK_TIERS[i]) {
                gained += award(userId, stage, BIZ_STREAK, dayKey + "_" + STREAK_TIERS[i], bonuses[i],
                        "连续学习 " + STREAK_TIERS[i] + " 天");
            }
        }
        return gained;
    }

    /** 今日已获积分（只统计受每日上限约束的「可重复」来源），供学生端展示上限进度 */
    public int todayCappedPoints(String userId) {
        if (StringTools.isEmpty(userId)) {
            return 0;
        }
        return recordMapper.sumPointsByUserAndTimeRange(
                userId, dayStart(), dayEnd(), new ArrayList<>(DAILY_CAP_TYPES));
    }

    /** 每日积分上限（读 GAME.POINT_DAILY_CAP，缺失回落默认 200） */
    public int dailyCap() {
        return intConfig("POINT_DAILY_CAP", DEFAULT_DAILY_CAP);
    }

    // ====== 各学习事件对应的发分入口（规则统一在本组件持有，触发点只负责「把事件告诉它」） ======

    /** 课时通关测验通过：分值 = GAME.POINT_LESSON_QUIZ + 得分率加成（每 10% 加 1 分，满分 +10） */
    public int awardLessonQuiz(String userId, String stage, String lessonId, int scorePercent) {
        int base = intConfig("POINT_LESSON_QUIZ", DEFAULT_LESSON_QUIZ_POINTS);
        int bonus = Math.max(0, Math.min(100, scorePercent)) / 10;
        return award(userId, stage, BIZ_LESSON_QUIZ, lessonId, base + bonus, "通过课时通关测验");
    }

    /**
     * 学完课时（该课时**没有**配置通关测验时）：打开课时资源即算学完，同样给激励分。
     * bizId = 课时 ID，每课时只发一次；受每日上限约束（不能靠反复点资源刷分）。
     * 配了测验的课时不走这里——它的分由「通过通关测验」发放（见 awardLessonQuiz）。
     */
    public int awardLessonDone(String userId, String stage, String lessonId) {
        return award(userId, stage, BIZ_LESSON_DONE, lessonId,
                intConfig("POINT_LESSON_DONE", DEFAULT_LESSON_DONE_POINTS), "学完课时（该课时无通关测验）");
    }

    /** 学习路径节点快测通过（bizId = 路径节点 ID，每节点只奖一次） */
    public int awardPathTest(String userId, String stage, String pathItemId) {
        return award(userId, stage, BIZ_PATH_TEST, pathItemId,
                intConfig("POINT_PATH_TEST", DEFAULT_PATH_TEST_POINTS), "通过路径节点快测");
    }

    /**
     * 编程题通关（bizId = 题目 ID，首通计分、重复通关不计分）。
     * 看过答案按 30% 计分；「是否看过」以服务端 Redis 标记为准（见 markCodingAnswerUsed）。
     */
    public int awardCodingProblem(String userId, String stage, String problemId, int problemScore) {
        boolean usedAnswer = isCodingAnswerUsed(userId, problemId);
        int points = usedAnswer ? (int) Math.round(Math.max(problemScore, 0) * 0.3) : Math.max(problemScore, 0);
        return award(userId, stage, BIZ_CODING_PROBLEM, problemId, points,
                usedAnswer ? "通关编程题（看过答案，按 30% 计分）" : "通关编程题");
    }

    /** 完成绘本创作（bizId = 绘本资源 ID；受每日上限约束） */
    public int awardPictureBook(String userId, String stage, String resourceId) {
        return award(userId, stage, BIZ_PICTURE_BOOK, resourceId,
                intConfig("POINT_PICTURE_BOOK", DEFAULT_PICTURE_BOOK_POINTS), "完成绘本创作");
    }

    /** 知识页确认入库（bizId = 知识页 ID；受每日上限约束） */
    public int awardWikiConfirm(String userId, String stage, String docId) {
        return award(userId, stage, BIZ_WIKI_CONFIRM, docId,
                intConfig("POINT_WIKI_CONFIRM", DEFAULT_WIKI_CONFIRM_POINTS), "知识页确认入库");
    }

    /** 学完一个动画讲解（bizId = 动画资源 ID；受每日上限约束） */
    public int awardAnimation(String userId, String stage, String resourceId) {
        return award(userId, stage, BIZ_ANIMATION, resourceId,
                intConfig("POINT_ANIMATION", DEFAULT_ANIMATION_POINTS), "学完一个动画讲解");
    }

    // ====== 编程题「看过答案」标记（Redis，TTL 6 小时）：显示答案接口写入、判分发分时读取 ======

    private static final String CODING_ANSWER_USED_KEY = "point:coding:answerUsed:";

    /** 学生查看了某题的参考答案（由显示答案接口调用） */
    public void markCodingAnswerUsed(String userId, String problemId) {
        if (StringTools.isEmpty(userId) || StringTools.isEmpty(problemId)) {
            return;
        }
        try {
            redisComponent.setString(CODING_ANSWER_USED_KEY + userId + ":" + problemId, "1", 6, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("编程题看答案标记写入失败 userId={} problemId={}", userId, problemId, e);
        }
    }

    /** 该生是否查看过该题答案（读失败按未看过处理，不影响判分主流程） */
    private boolean isCodingAnswerUsed(String userId, String problemId) {
        if (StringTools.isEmpty(userId) || StringTools.isEmpty(problemId)) {
            return false;
        }
        try {
            return "1".equals(redisComponent.getString(CODING_ANSWER_USED_KEY + userId + ":" + problemId));
        } catch (Exception e) {
            log.warn("编程题看答案标记读取失败 userId={} problemId={}", userId, problemId, e);
            return false;
        }
    }

    /**
     * 知识点「已掌握」奖励（触发点：{@link KnowledgeMasteryComponent} 跨入已掌握时调用）。
     * 规则由本组件持有（分值读 GAME.POINT_MASTERY），调用方只负责「把事件告诉它」。
     * 幂等键 = 知识点 ID → 同一知识点只奖一次（掉出掌握线再回来不重复发）。
     */
    @Transactional(rollbackFor = Exception.class)
    public int awardMastery(String userId, String stage, String knowledgePointId) {
        if (StringTools.isEmpty(knowledgePointId)) {
            return 0;
        }
        return award(userId, stage, BIZ_MASTERY, knowledgePointId,
                intConfig("POINT_MASTERY", DEFAULT_MASTERY_POINTS), "掌握知识点");
    }

    /**
     * 徽章判定：遍历启用中的（本学段可见）徽章，命中规则且尚未解锁的即时解锁并发奖励分。
     *
     * 规则类型与判定数据源：
     * - TOTAL_POINTS / STREAK：读账户；
     * - FIRST_PASS / MASTERY_COUNT / CODING_COUNT / CREATION：按流水条数统计（每次发放都是一条幂等流水，
     *   因此「流水条数」就是「事件次数」，无需跨表 join）；
     * - COMBO_MAX：连击是会话内计数，等练习链路把「最高连击」写进流水后再支持（A-4 待办）。
     */
    private void evaluateBadges(String userId, String stage, StudentPointAccount account) {
        GameBadgeQuery query = new GameBadgeQuery();
        query.setStatus(1);
        query.setStage(stage);
        List<GameBadge> badges = badgeMapper.selectListByParam(query);
        if (badges == null || badges.isEmpty()) {
            return;
        }
        Set<String> unlocked = new HashSet<>(badgeRecordMapper.selectBadgeIdsByUser(userId));
        for (GameBadge badge : badges) {
            if (unlocked.contains(badge.getBadgeId()) || !ruleReached(badge, userId, account)) {
                continue;
            }
            StudentBadgeRecord badgeRecord = new StudentBadgeRecord();
            badgeRecord.setUserId(userId);
            badgeRecord.setBadgeId(badge.getBadgeId());
            if (badgeRecordMapper.insertIgnore(badgeRecord) == 0) {
                continue;
            }
            log.info("徽章解锁 userId={} badge={} name={}", userId, badge.getBadgeId(), badge.getName());
            if (badge.getRewardPoints() != null && badge.getRewardPoints() > 0) {
                award(userId, stage, BIZ_BADGE, badge.getBadgeId(), badge.getRewardPoints(),
                        "解锁徽章《" + badge.getName() + "》");
            }
        }
    }

    /** 徽章规则是否达成 */
    private boolean ruleReached(GameBadge badge, String userId, StudentPointAccount account) {
        int threshold = badge.getRuleValue() == null ? 0 : badge.getRuleValue();
        String ruleType = badge.getRuleType() == null ? "" : badge.getRuleType();
        switch (ruleType) {
            case "TOTAL_POINTS":
                return (account.getTotalPoints() == null ? 0 : account.getTotalPoints()) >= threshold;
            case "STREAK":
                return (account.getStreakDays() == null ? 0 : account.getStreakDays()) >= threshold;
            case "FIRST_PASS":
                return recordMapper.countByUserAndBizTypes(userId, PASS_BIZ_TYPES) >= Math.max(threshold, 1);
            case "MASTERY_COUNT":
                return recordMapper.countByUserAndBizTypes(userId, List.of(BIZ_MASTERY)) >= threshold;
            case "CODING_COUNT":
                return recordMapper.countByUserAndBizTypes(userId, List.of(BIZ_CODING_PROBLEM)) >= threshold;
            case "CREATION":
                return recordMapper.countByUserAndBizTypes(userId, CREATION_BIZ_TYPES) >= threshold;
            case "COMBO_MAX":
                return currentCombo(userId) >= Math.max(threshold, 1);
            case "PATH_DONE_COUNT":
                return recordMapper.countByUserAndBizTypes(userId, List.of(BIZ_PATH_DONE)) >= Math.max(threshold, 1);
            default:
                log.debug("未支持的徽章规则类型 ruleType={} badge={}", ruleType, badge.getBadgeId());
                return false;
        }
    }

    /**
     * 徽章墙（A-4）：本学段可见徽章 + 我的解锁状态与进度。
     *
     * 进度与解锁判定共用同一套规则（ruleReached），避免出现「进度已满却没发徽章」的展示/发放不一致。
     * 小学段同样开放徽章（纯正向激励），只有排行榜对小学段关闭（见 A-10 学段适配）。
     */
    public List<PointBadgeVO> listBadges(String userId, String stage) {
        List<PointBadgeVO> result = new ArrayList<>();
        if (StringTools.isEmpty(userId)) {
            return result;
        }
        GameBadgeQuery query = new GameBadgeQuery();
        query.setStatus(1);
        query.setStage(stage);
        List<GameBadge> badges = badgeMapper.selectListByParam(query);
        if (badges == null || badges.isEmpty()) {
            return result;
        }
        StudentPointAccount account = accountMapper.selectByUserId(userId);
        Map<String, Date> unlockedTime = new HashMap<>();
        List<StudentBadgeRecord> unlockedRecords = badgeRecordMapper.selectByUser(userId);
        if (unlockedRecords != null) {
            for (StudentBadgeRecord record : unlockedRecords) {
                unlockedTime.put(record.getBadgeId(), record.getCreateTime());
            }
        }
        for (GameBadge badge : badges) {
            int threshold = badge.getRuleValue() == null ? 0 : badge.getRuleValue();
            boolean unlocked = unlockedTime.containsKey(badge.getBadgeId());
            int progress = unlocked ? threshold : ruleProgress(badge, userId, account);
            PointBadgeVO vo = new PointBadgeVO();
            vo.setBadgeId(badge.getBadgeId());
            vo.setName(badge.getName());
            vo.setDescription(badge.getDescription());
            vo.setIcon(badge.getIcon());
            vo.setRuleType(badge.getRuleType());
            vo.setRuleValue(threshold);
            vo.setRewardPoints(badge.getRewardPoints());
            vo.setUnlocked(unlocked);
            vo.setUnlockedTime(unlockedTime.get(badge.getBadgeId()));
            vo.setProgress(progress);
            vo.setProgressText(progressText(badge.getRuleType(), progress, threshold));
            result.add(vo);
        }
        return result;
    }

    /** 徽章当前进度值（与 ruleReached 同一口径，仅供展示） */
    private int ruleProgress(GameBadge badge, String userId, StudentPointAccount account) {
        String ruleType = badge.getRuleType() == null ? "" : badge.getRuleType();
        return switch (ruleType) {
            case "TOTAL_POINTS" -> account == null || account.getTotalPoints() == null ? 0 : account.getTotalPoints();
            case "STREAK" -> account == null || account.getStreakDays() == null ? 0 : account.getStreakDays();
            case "FIRST_PASS" -> recordMapper.countByUserAndBizTypes(userId, PASS_BIZ_TYPES);
            case "MASTERY_COUNT" -> recordMapper.countByUserAndBizTypes(userId, List.of(BIZ_MASTERY));
            case "CODING_COUNT" -> recordMapper.countByUserAndBizTypes(userId, List.of(BIZ_CODING_PROBLEM));
            case "CREATION" -> recordMapper.countByUserAndBizTypes(userId, CREATION_BIZ_TYPES);
            case "COMBO_MAX" -> currentCombo(userId);
            case "PATH_DONE_COUNT" -> recordMapper.countByUserAndBizTypes(userId, List.of(BIZ_PATH_DONE));
            default -> 0;
        };
    }

    /** 进度文案：`3/7 天`；规则未接入（COMBO_MAX）返回空串由前端兜底 */
    private String progressText(String ruleType, int progress, int threshold) {
        String unit = switch (ruleType == null ? "" : ruleType) {
            case "STREAK" -> "天";
            case "TOTAL_POINTS" -> "分";
            case "CODING_COUNT" -> "道";
            case "FIRST_PASS" -> "次";
            case "MASTERY_COUNT", "CREATION" -> "个";
            case "COMBO_MAX" -> "连对";
            case "PATH_DONE_COUNT" -> "条";
            default -> null;
        };
        return unit == null ? "" : Math.min(progress, threshold) + "/" + threshold + " " + unit;
    }

    /**
     * 学习路径节点完成奖励（二期 PATH）：节点刚跨入「已掌握」时发一次，幂等键 = 节点 ID。
     * 调用点在 LearningPathComponent 的状态刷新里，异常只记日志，绝不影响路径状态本身。
     */
    public int awardPathNode(String userId, String stage, String itemId, String itemName) {
        if (StringTools.isEmpty(userId) || StringTools.isEmpty(itemId)) {
            return 0;
        }
        int points = intConfig("POINT_PATH_NODE", DEFAULT_PATH_NODE_POINTS);
        if (points <= 0) {
            return 0;
        }
        return award(userId, stage, BIZ_PATH_NODE, itemId, points,
                "完成学习路径节点" + (StringTools.isEmpty(itemName) ? "" : "《" + itemName + "》"));
    }

    /**
     * 整条学习路径完成奖励（二期 PATH）：路径刚变为「已完成」时发一次，幂等键 = 路径 ID；
     * 发分同时触发徽章判定，「路径通学者」（PATH_DONE_COUNT≥1）随之一并解锁。
     */
    public int awardPathDone(String userId, String stage, String pathId, String pathTitle) {
        if (StringTools.isEmpty(userId) || StringTools.isEmpty(pathId)) {
            return 0;
        }
        int points = intConfig("POINT_PATH_DONE", DEFAULT_PATH_DONE_POINTS);
        if (points <= 0) {
            return 0;
        }
        return award(userId, stage, BIZ_PATH_DONE, pathId, points,
                "走完学习路径" + (StringTools.isEmpty(pathTitle) ? "" : "《" + pathTitle + "》"));
    }

    /**
     * 当前连击（连对题数，二期 COMBO）：从 practice_record 按时间倒序数连续答对，
     * 遇到第一条答错即停止。不落库、不加字段，避免计数器漂移。
     */
    public int currentCombo(String userId) {
        if (StringTools.isEmpty(userId)) {
            return 0;
        }
        List<Integer> flags = practiceComboMapper.selectRecentIsCorrect(userId, COMBO_SCAN_LIMIT);
        if (flags == null || flags.isEmpty()) {
            return 0;
        }
        int combo = 0;
        for (Integer flag : flags) {
            if (flag != null && flag == 1) {
                combo++;
            } else {
                break;
            }
        }
        return combo;
    }

    /**
     * 连击奖励（练习链路在**答对**落库后调用）：连对达到档位时发一次奖励，档内继续连对不重复发。
     *
     * 幂等键 = 触发的那条 practice_record 主键（同一条作答不会重复发），
     * 因此连对到 3/5/10 各发一次，第 4、11 题不会再发。发分同时会触发徽章判定，
     * 「十全十美」（COMBO_MAX≥10）随之一并解锁。
     *
     * @return 本次实际发放的积分（0 = 未到档位/已发过）
     */
    public int awardCombo(String userId, String stage, String practiceRecordId) {
        if (StringTools.isEmpty(userId) || StringTools.isEmpty(practiceRecordId)) {
            return 0;
        }
        int combo = currentCombo(userId);
        int tierIndex = -1;
        for (int i = 0; i < COMBO_TIERS.length; i++) {
            if (combo == COMBO_TIERS[i]) {
                tierIndex = i;
                break;
            }
        }
        if (tierIndex < 0) {
            return 0;
        }
        int[] bonus = comboBonus();
        int points = tierIndex < bonus.length ? bonus[tierIndex] : bonus[bonus.length - 1];
        if (points <= 0) {
            return 0;
        }
        return award(userId, stage, BIZ_COMBO, practiceRecordId, points,
                "连续答对 " + combo + " 题");
    }

    /** 连击奖励档位奖励值（GAME.POINT_COMBO，逗号分隔；与 COMBO_TIERS 一一对应） */
    private int[] comboBonus() {
        String raw = systemConfigComponent.getValue(SystemConfigComponent.GROUP_GAME, "POINT_COMBO", null);
        if (raw == null || raw.isBlank()) {
            return DEFAULT_COMBO_BONUS;
        }
        try {
            String[] parts = raw.trim().split(",");
            int[] parsed = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                parsed[i] = Integer.parseInt(parts[i].trim());
            }
            return parsed.length > 0 ? parsed : DEFAULT_COMBO_BONUS;
        } catch (NumberFormatException e) {
            log.warn("GAME.POINT_COMBO 解析失败，回落默认值：{}", raw);
            return DEFAULT_COMBO_BONUS;
        }
    }

    // ====== 配置读取（GAME 组；缺失回落代码默认值） ======


    private int intConfig(String key, int defaultValue) {
        String raw = systemConfigComponent.getValue(SystemConfigComponent.GROUP_GAME, key, null);
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            log.warn("GAME.{} 解析失败，回落默认值 {}：{}", key, defaultValue, raw);
            return defaultValue;
        }
    }

    private int[] intArrayConfig(String key, int[] defaultValue) {
        String raw = systemConfigComponent.getValue(SystemConfigComponent.GROUP_GAME, key, null);
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        try {
            String[] parts = raw.trim().split(",");
            int[] parsed = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                parsed[i] = Integer.parseInt(parts[i].trim());
            }
            return parsed;
        } catch (NumberFormatException e) {
            log.warn("GAME.{} 解析失败，回落默认值：{}", key, raw);
            return defaultValue;
        }
    }

    private Date dayStart() {
        return Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Date dayEnd() {
        return Date.from(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
