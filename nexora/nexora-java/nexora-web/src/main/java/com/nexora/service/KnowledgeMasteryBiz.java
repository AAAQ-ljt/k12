package com.nexora.service;

import com.nexora.entity.enums.DateTimePatternEnum;
import com.nexora.entity.po.KnowledgeMastery;
import com.nexora.entity.po.KnowledgePoint;
import com.nexora.entity.po.LearningPathItem;
import com.nexora.entity.po.PracticeRecord;
import com.nexora.entity.query.KnowledgeMasteryQuery;
import com.nexora.entity.query.KnowledgePointQuery;
import com.nexora.entity.query.LearningPathItemQuery;
import com.nexora.entity.query.PracticeRecordQuery;
import com.nexora.service.LearningPathItemService;
import com.nexora.service.PracticeRecordService;
import com.nexora.utils.DateUtil;
import com.nexora.utils.StringTools;
import com.nexora.vo.KnowledgeMasteryItemVO;
import com.nexora.vo.KnowledgeMasteryOverviewVO;
import com.nexora.vo.LearningTrendVO;
import com.nexora.vo.ReviewLocateVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 学生端学习进度（知识点掌握度概览）业务：
 * 数据全部来自判分链路回写的 knowledge_mastery（见 KnowledgeMasteryComponent），
 * 供个性化学习路径页展示真实进度与待复习提醒。
 */
@Service
public class KnowledgeMasteryBiz {

    /** 明细默认返回条数 */
    private static final int DEFAULT_LIMIT = 20;

    /** 明细上限 */
    private static final int MAX_LIMIT = 100;

    @Resource
    private KnowledgeMasteryService knowledgeMasteryService;

    @Resource
    private KnowledgePointService knowledgePointService;

    @Resource
    private PracticeRecordService practiceRecordService;

    @Resource
    private LearningPathItemService learningPathItemService;

    /** 按北京时间（GMT+8）归属学习日 */
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("MM-dd");

    public KnowledgeMasteryOverviewVO myOverview(String userId, Integer limit) {
        List<KnowledgeMastery> list = new ArrayList<>();
        if (!StringTools.isEmpty(userId)) {
            KnowledgeMasteryQuery query = new KnowledgeMasteryQuery();
            query.setUserId(userId);
            query.setOrderBy("last_practice_time desc");
            List<KnowledgeMastery> found = knowledgeMasteryService.findListByParam(query);
            if (found != null) {
                list = found;
            }
        }
        Map<String, String> pointNames = loadKnowledgePointNames();

        KnowledgeMasteryOverviewVO vo = new KnowledgeMasteryOverviewVO();
        int mastered = 0;
        int learning = 0;
        int scoreSum = 0;
        int practiceSum = 0;
        int correctSum = 0;
        int dueCount = 0;
        Date now = new Date();
        List<KnowledgeMasteryItemVO> items = new ArrayList<>();
        int size = limit == null || limit <= 0 ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);
        for (KnowledgeMastery mastery : list) {
            int score = mastery.getMasteryScore() == null ? 0 : mastery.getMasteryScore();
            int status = mastery.getStatus() == null ? 0 : mastery.getStatus();
            int practice = mastery.getPracticeCount() == null ? 0 : mastery.getPracticeCount();
            int correct = mastery.getCorrectCount() == null ? 0 : mastery.getCorrectCount();
            scoreSum += score;
            practiceSum += practice;
            correctSum += correct;
            if (status == 2) {
                mastered++;
            } else if (status == 1) {
                learning++;
            }
            boolean due = mastery.getNextReviewTime() != null && !mastery.getNextReviewTime().after(now);
            if (due) {
                dueCount++;
            }
            if (items.size() < size) {
                KnowledgeMasteryItemVO item = new KnowledgeMasteryItemVO();
                item.setKnowledgePointId(mastery.getKnowledgePointId());
                String name = pointNames.get(mastery.getKnowledgePointId());
                item.setKnowledgePointName(StringTools.isEmpty(name) ? mastery.getKnowledgePointId() : name);
                item.setStage(mastery.getStage());
                item.setMasteryScore(score);
                item.setStatus(status);
                item.setPracticeCount(practice);
                item.setCorrectCount(correct);
                item.setLastPracticeTime(formatTime(mastery.getLastPracticeTime()));
                item.setNextReviewTime(formatTime(mastery.getNextReviewTime()));
                item.setDue(due);
                items.add(item);
            }
        }

        vo.setMasteredCount(mastered);
        vo.setLearningCount(learning);
        vo.setTotalCount(list.size());
        vo.setAvgMasteryScore(list.isEmpty() ? 0 : (int) Math.round(scoreSum * 1.0 / list.size()));
        vo.setTotalPractice(practiceSum);
        vo.setTotalCorrect(correctSum);
        vo.setCorrectRate(practiceSum == 0 ? 0 : (int) Math.round(correctSum * 100.0 / practiceSum));
        vo.setDueReviewCount(dueCount);
        vo.setItems(items);
        return vo;
    }

    /**
     * 知识点ID → 名称：知识点表体量小，一次查出建映射，避免逐条查库
     */
    private Map<String, String> loadKnowledgePointNames() {
        Map<String, String> names = new HashMap<>();
        try {
            List<KnowledgePoint> points = knowledgePointService.findListByParam(new KnowledgePointQuery());
            if (points != null) {
                for (KnowledgePoint point : points) {
                    names.put(point.getKnowledgePointId(), point.getName());
                }
            }
        } catch (Exception e) {
            // 名称缺失不影响进度展示（回落知识点ID）
        }
        return names;
    }

    private String formatTime(Date date) {
        if (date == null) {
            return null;
        }
        return DateUtil.format(date, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern());
    }

    /**
     * 我的页面学习趋势：一次查出该生全部练习流水，内存聚合学习天数/连续天数/近 7 天分布
     */
    public LearningTrendVO loadMyTrend(String userId) {
        LearningTrendVO vo = new LearningTrendVO();
        if (StringTools.isEmpty(userId)) {
            return vo;
        }
        PracticeRecordQuery query = new PracticeRecordQuery();
        query.setUserId(userId);
        List<PracticeRecord> records = practiceRecordService.findListByParam(query);
        if (records == null || records.isEmpty()) {
            vo.setWeekTrend(buildEmptyWeek());
            return vo;
        }
        vo.setTotalPractice(records.size());

        // 学习日集合（一次遍历，避免逐条查库）
        Set<LocalDate> days = new HashSet<>();
        Map<LocalDate, Integer> dayCount = new HashMap<>();
        for (PracticeRecord record : records) {
            if (record.getCreateTime() == null) {
                continue;
            }
            LocalDate day = LocalDate.ofInstant(record.getCreateTime().toInstant(), ZONE);
            days.add(day);
            dayCount.merge(day, 1, Integer::sum);
        }
        vo.setStudyDays(days.size());

        // 连续学习天数：今天有练习从今天起算，否则从昨天起算
        LocalDate today = LocalDate.now(ZONE);
        int streak = 0;
        LocalDate cursor = days.contains(today) ? today : today.minusDays(1);
        while (days.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        vo.setStreakDays(streak);

        // 近 7 天（含今天）每日练习数
        List<LearningTrendVO.WeekDayVO> weekTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            LearningTrendVO.WeekDayVO item = new LearningTrendVO.WeekDayVO();
            item.setDay(day.format(DAY_FORMAT));
            item.setCount(dayCount.getOrDefault(day, 0));
            weekTrend.add(item);
        }
        vo.setWeekTrend(weekTrend);
        return vo;
    }

    private List<LearningTrendVO.WeekDayVO> buildEmptyWeek() {
        List<LearningTrendVO.WeekDayVO> weekTrend = new ArrayList<>();
        LocalDate today = LocalDate.now(ZONE);
        for (int i = 6; i >= 0; i--) {
            LearningTrendVO.WeekDayVO item = new LearningTrendVO.WeekDayVO();
            item.setDay(today.minusDays(i).format(DAY_FORMAT));
            item.setCount(0);
            weekTrend.add(item);
        }
        return weekTrend;
    }

    /**
     * 待复习知识点定位：按知识点在用户的路径节点中匹配（取最新一条），
     * 命中则前端跳路线做节点快测；否则前端转 AI 助教对话复习
     */
    public ReviewLocateVO locateReview(String userId, String knowledgePointId) {
        ReviewLocateVO vo = new ReviewLocateVO();
        vo.setLocated(false);
        if (StringTools.isEmpty(userId) || StringTools.isEmpty(knowledgePointId)) {
            return vo;
        }
        LearningPathItemQuery query = new LearningPathItemQuery();
        query.setUserId(userId);
        query.setKnowledgePointId(knowledgePointId.trim());
        query.setOrderBy("create_time desc");
        List<LearningPathItem> items = learningPathItemService.findListByParam(query);
        if (items == null || items.isEmpty()) {
            return vo;
        }
        LearningPathItem item = items.get(0);
        vo.setLocated(true);
        vo.setPathId(item.getPathId());
        vo.setItemId(item.getItemId());
        vo.setKnowledgePointName(item.getKnowledgePointName());
        return vo;
    }
}
