package com.nexora.service;

import com.nexora.entity.enums.DateTimePatternEnum;
import com.nexora.entity.po.KnowledgeMastery;
import com.nexora.entity.po.KnowledgePoint;
import com.nexora.entity.query.KnowledgeMasteryQuery;
import com.nexora.entity.query.KnowledgePointQuery;
import com.nexora.utils.DateUtil;
import com.nexora.utils.StringTools;
import com.nexora.vo.KnowledgeMasteryItemVO;
import com.nexora.vo.KnowledgeMasteryOverviewVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
}
