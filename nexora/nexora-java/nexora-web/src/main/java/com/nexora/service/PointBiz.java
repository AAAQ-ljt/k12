package com.nexora.service;

import com.nexora.component.PointAwardComponent;
import com.nexora.component.PointExchangeComponent;
import com.nexora.component.PointLevelComponent;
import com.nexora.entity.po.StudentPointAccount;
import com.nexora.entity.po.StudentPointRecord;
import com.nexora.entity.query.StudentPointRecordQuery;
import com.nexora.entity.vo.PointAccountVO;
import com.nexora.entity.vo.PointBadgeVO;
import com.nexora.entity.vo.PointExchangeListVO;
import com.nexora.entity.vo.PointExchangeResultVO;
import com.nexora.entity.vo.PointRankItemVO;
import com.nexora.entity.vo.PointRankResultVO;
import com.nexora.mappers.StudentPointAccountMapper;
import com.nexora.mappers.StudentPointRecordMapper;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * 学生积分查询编排（只读；写入统一走 {@link PointAwardComponent}，与掌握度组件的分工一致）。
 */
@Service
public class PointBiz {

    /** 明细列表单次最多返回条数（学生端展示最近记录，分页留到管理端流水页） */
    private static final int RECORD_LIMIT_DEFAULT = 20;
    private static final int RECORD_LIMIT_MAX = 100;

    /** 排行榜展示条数上限（只展示前排，不提供全量榜单导出） */
    private static final int RANK_LIMIT = 20;

    @Resource
    private StudentPointAccountMapper accountMapper;

    @Resource
    private StudentPointRecordMapper recordMapper;

    @Resource
    private PointLevelComponent levelComponent;

    @Resource
    private PointAwardComponent pointAwardComponent;

    @Resource
    private PointExchangeComponent pointExchangeComponent;

    /** 我的积分账户概览（账户不存在时返回零值，等级按 1 级起算） */
    public PointAccountVO myAccount(String userId, String stage) {
        StudentPointAccount account = userId == null ? null : accountMapper.selectByUserId(userId);
        int total = account == null || account.getTotalPoints() == null ? 0 : account.getTotalPoints();
        PointAccountVO vo = new PointAccountVO();
        vo.setTotalPoints(total);
        vo.setAvailablePoints(account == null || account.getAvailablePoints() == null ? 0 : account.getAvailablePoints());
        int level = levelComponent.levelOf(total);
        vo.setLevel(level);
        // 段位名按登录态学段折算（账户尚未建立时也能给出正确称谓）
        String effectiveStage = StringTools.isEmpty(stage) ? (account == null ? null : account.getStage()) : stage;
        vo.setLevelName(levelComponent.levelName(effectiveStage, level));
        vo.setLevelFloor(levelComponent.levelFloor(level));
        vo.setNextLevelPoints(levelComponent.pointsToNextLevel(total));
        vo.setStreakDays(account == null || account.getStreakDays() == null ? 0 : account.getStreakDays());
        vo.setTodayPoints(pointAwardComponent.todayCappedPoints(userId));
        vo.setDailyCap(pointAwardComponent.dailyCap());
        return vo;
    }

    /** 我的积分明细（按时间倒序，最多取 RECORD_LIMIT_MAX 条） */
    public List<StudentPointRecord> myRecords(String userId, String bizType,
                                             String createTimeStart, String createTimeEnd, Integer pageSize) {
        StudentPointRecordQuery query = new StudentPointRecordQuery();
        query.setUserId(userId);
        query.setBizType(bizType);
        query.setCreateTimeStart(createTimeStart);
        query.setCreateTimeEnd(createTimeEnd);
        int limit = pageSize == null || pageSize <= 0 ? RECORD_LIMIT_DEFAULT : Math.min(pageSize, RECORD_LIMIT_MAX);
        query.setPageSize(limit);
        return recordMapper.selectListByParam(query);
    }

    /** 我的徽章墙（徽章 + 解锁状态 + 进度） */
    public List<PointBadgeVO> myBadges(String userId, String stage) {
        return pointAwardComponent.listBadges(userId, stage);
    }

    /** 兑换页：可用积分 + 商品（上传额度扩容 / 音色解锁）+ 我的兑换记录 */
    public PointExchangeListVO exchangeCatalog(String userId, String stage) {
        return pointExchangeComponent.catalog(userId, stage);
    }

    /** 兑换权益（可用积分扣减；累计积分不变） */
    public PointExchangeResultVO exchange(String userId, String stage, String itemCode) {
        return pointExchangeComponent.exchange(userId, stage, itemCode);
    }

    /**
     * 排行榜（A-5）：本学段「累计榜 / 周榜」+ 我的名次。
     *
     * 口径：
     * 1) 小学段（小低/小高）不开放排行榜 —— 只保留星星与徽章的正向激励，返回 enabled=false；
     * 2) 只排本学段，不做全站混排（避免小学生和高中生同榜）；
     * 3) 昵称脱敏后外发（见 maskName），本人一行标记 me 并显示「我」；
     * 4) 编程比赛成绩是独立体系、不进全局积分，因此不会出现在这里（见规划 A-5）。
     */
    public PointRankResultVO rank(String userId, String stage, String type) {
        boolean week = "week".equalsIgnoreCase(type);
        PointRankResultVO vo = new PointRankResultVO();
        vo.setType(week ? "week" : "total");
        vo.setScope("stage");
        if (levelComponent.starStage(stage)) {
            vo.setEnabled(false);
            vo.setTip("小学阶段用星星记录成长，暂不开放排行榜");
            return vo;
        }
        vo.setEnabled(true);
        Date weekStart = week ? weekStart() : null;
        List<PointRankItemVO> rows = accountMapper.selectRankRows(stage, weekStart, RANK_LIMIT);
        int rankNo = 0;
        for (PointRankItemVO row : rows) {
            row.setRankNo(++rankNo);
            boolean me = row.getUserId() != null && row.getUserId().equals(userId);
            row.setMe(me);
            row.setDisplayName(me ? "我" : maskName(row.getNickName(), row.getUserId()));
            row.setLevelName(levelComponent.levelName(row.getStage(), row.getLevel()));
            // 脱敏后即清空原始标识，接口只回展示名（学生端拿不到别人的 userId / 昵称）
            row.setUserId(null);
            row.setNickName(null);
        }
        vo.setList(rows);
        if (StringTools.isEmpty(userId)) {
            return vo;
        }
        // 我的名次：能进 TOP-N 已在榜上，否则按「严格高于我的学生数 + 1」算
        PointRankItemVO mine = rows.stream().filter(r -> Boolean.TRUE.equals(r.getMe())).findFirst().orElse(null);
        if (mine != null) {
            vo.setMyRank(mine.getRankNo());
            vo.setMyPoints(mine.getPoints());
            return vo;
        }
        int myPoints = week ? weekPoints(userId) : myTotalPoints(userId);
        vo.setMyPoints(myPoints);
        if (myPoints <= 0) {
            return vo;
        }
        int above = week ? recordMapper.countAboveWeek(stage, weekStart, myPoints)
                : accountMapper.countAboveTotal(stage, myPoints);
        vo.setMyRank(above + 1);
        return vo;
    }

    /** 展示名脱敏：昵称首字符 + *** + 学号后 4 位（自己认得出、别人认不出，且不会重名撞车） */
    private String maskName(String nickName, String userId) {
        String tail = StringTools.isEmpty(userId) || userId.length() < 4
                ? "" : userId.substring(userId.length() - 4);
        String head = StringTools.isEmpty(nickName) ? "同学" : nickName.substring(0, 1);
        return head + "***" + tail;
    }

    /** 本周一 00:00（周榜起点，按服务器本地时区） */
    private Date weekStart() {
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        return Date.from(monday.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /** 我的本周净得积分（周榜口径） */
    private int weekPoints(String userId) {
        Date start = weekStart();
        Date end = Date.from(start.toInstant().plus(Duration.ofDays(7)).minusSeconds(1));
        return recordMapper.sumPointsByUserAndTimeRange(userId, start, end, null);
    }

    /** 我的累计积分 */
    private int myTotalPoints(String userId) {
        StudentPointAccount account = accountMapper.selectByUserId(userId);
        return account == null || account.getTotalPoints() == null ? 0 : account.getTotalPoints();
    }
}
