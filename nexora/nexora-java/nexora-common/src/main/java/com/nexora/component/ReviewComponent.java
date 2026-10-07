package com.nexora.component;

import com.nexora.entity.po.KnowledgeMastery;
import com.nexora.entity.vo.KnowledgeMasteryVO;
import com.nexora.mappers.KnowledgeMasteryMapper;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Calendar;
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
    public void muteToday(String userId, String knowledgePointId) {
        updateMute(userId, knowledgePointId, 0, tomorrowStart());
    }

    /** 「不再提醒这个知识点」：长期静音（可恢复） */
    public void muteForever(String userId, String knowledgePointId) {
        updateMute(userId, knowledgePointId, 1, null);
    }

    /** 恢复提醒 */
    public void unmute(String userId, String knowledgePointId) {
        updateMute(userId, knowledgePointId, 0, null);
    }

    private void updateMute(String userId, String knowledgePointId, Integer muted, Date muteUntil) {
        if (StringTools.isEmpty(userId) || StringTools.isEmpty(knowledgePointId)) {
            return;
        }
        try {
            knowledgeMasteryMapper.updateReviewMute(userId, knowledgePointId, muted, muteUntil);
            log.info("复习提醒静音更新 userId={} pointId={} muted={} until={}", userId, knowledgePointId, muted, muteUntil);
        } catch (Exception e) {
            log.warn("复习静音更新失败 userId={} pointId={}", userId, knowledgePointId, e);
        }
    }

    private Date tomorrowStart() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}
