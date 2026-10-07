package com.nexora.component;

import com.nexora.entity.po.KnowledgeMastery;
import com.nexora.entity.vo.KnowledgeMasteryVO;
import com.nexora.mappers.KnowledgeMasteryMapper;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * 复习判定与静音（复习闭环设计点①④，唯一口径）。
 *
 * **「待复习」的判定只有这一处**：掌握度 `next_review_time` 早于当前时间，且未被静音。
 * 路径页、管理端、AI 工具都用它，避免各算一套出现"页面说逾期、AI 说没到期"的自相矛盾。
 *
 * 静音两种粒度：
 * - 「今天不用提醒」→ 写 `mute_until` = 明天 0 点（到点自动恢复提醒）；
 * - 「不再提醒这个知识点」→ `muted = 1`（长期，学生/管理端可恢复）。
 * 静音项不计入待复习计数，但掌握度详情里仍然可见（不隐藏事实）。
 */
@Slf4j
@Component
public class ReviewComponent {

    /** 统计与复习排期统一用东八区（与学习趋势、JDBC serverTimezone=GMT+8 保持一致） */
    private static final ZoneId ZONE_SHANGHAI = ZoneId.of("Asia/Shanghai");

    @Resource
    private KnowledgeMasteryMapper knowledgeMasteryMapper;

    /** 是否处于静音中（长期静音，或临时静音尚未到期） */
    public boolean isMuted(KnowledgeMastery mastery) {
        if (mastery == null) {
            return false;
        }
        if (mastery.getMuted() != null && mastery.getMuted() == 1) {
            return true;
        }
        Date muteUntil = mastery.getMuteUntil();
        return muteUntil != null && muteUntil.after(new Date());
    }

    /** 是否待复习（唯一判定口径）：下次复习时间已过且未静音 */
    public boolean isDue(KnowledgeMastery mastery) {
        if (mastery == null || mastery.getNextReviewTime() == null) {
            return false;
        }
        return mastery.getNextReviewTime().before(new Date()) && !isMuted(mastery);
    }

    /** 是否处于静音中（VO 版：学习分析接口返回的掌握度视图） */
    public boolean isMuted(KnowledgeMasteryVO mastery) {
        if (mastery == null) {
            return false;
        }
        if (mastery.getMuted() != null && mastery.getMuted() == 1) {
            return true;
        }
        Date muteUntil = mastery.getMuteUntil();
        return muteUntil != null && muteUntil.after(new Date());
    }

    /** 是否待复习（VO 版）：下次复习时间已过且未静音 */
    public boolean isDue(KnowledgeMasteryVO mastery) {
        if (mastery == null || mastery.getNextReviewTime() == null) {
            return false;
        }
        return mastery.getNextReviewTime().before(new Date()) && !isMuted(mastery);
    }

    /** 逾期天数（未逾期返回 0） */
    public long overdueDays(KnowledgeMastery mastery) {
        if (mastery == null || mastery.getNextReviewTime() == null) {
            return 0;
        }
        long days = (System.currentTimeMillis() - mastery.getNextReviewTime().getTime()) / 86400000L;
        return Math.max(days, 0);
    }

    /** 「今天不用提醒」：临时静音到明天 0 点 */
    public boolean muteToday(String userId, String knowledgePointId) {
        return updateMute(userId, knowledgePointId, 0, tomorrowStart());
    }

    /** 「不再提醒这个知识点」：长期静音（可恢复） */
    public boolean muteForever(String userId, String knowledgePointId) {
        return updateMute(userId, knowledgePointId, 1, null);
    }

    /** 恢复提醒 */
    public boolean unmute(String userId, String knowledgePointId) {
        return updateMute(userId, knowledgePointId, 0, null);
    }

    /**
     * 更新静音状态。
     *
     * @return true = 已写入；false = 写入失败或没有该掌握度记录（调用方要如实告诉学生，
     *         否则界面提示「今天不再提醒」，而列表里该知识点依然挂着「该复习了」，2026-10-08 修）
     */
    private boolean updateMute(String userId, String knowledgePointId, Integer muted, Date muteUntil) {
        if (StringTools.isEmpty(userId) || StringTools.isEmpty(knowledgePointId)) {
            return false;
        }
        try {
            Integer rows = knowledgeMasteryMapper.updateReviewMute(userId, knowledgePointId, muted, muteUntil);
            if (rows == null || rows <= 0) {
                log.warn("复习静音未生效（无对应掌握度记录）userId={} pointId={}", userId, knowledgePointId);
                return false;
            }
            log.info("复习提醒静音更新 userId={} pointId={} muted={} until={}", userId, knowledgePointId, muted, muteUntil);
            return true;
        } catch (Exception e) {
            log.error("复习静音更新失败 userId={} pointId={}（需提示学生）", userId, knowledgePointId, e);
            return false;
        }
    }

    private Date tomorrowStart() {
        // 与统计口径（学习趋势按 Asia/Shanghai 分日）保持一致：
        // 若 JVM 时区不是东八区，用系统默认时区算出的「明天 0 点」会偏移，
        // 例如 UTC 下等于北京时间次日 8 点，会把明天上午的提醒一起吞掉
        ZonedDateTime tomorrow = ZonedDateTime.now(ZONE_SHANGHAI)
                .plusDays(1)
                .truncatedTo(ChronoUnit.DAYS);
        return Date.from(tomorrow.toInstant());
    }
}
