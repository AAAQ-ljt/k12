package com.nexora.service;

import com.nexora.component.PointAwardComponent;
import com.nexora.component.PointLevelComponent;
import com.nexora.entity.po.StudentPointAccount;
import com.nexora.entity.po.StudentPointRecord;
import com.nexora.entity.query.StudentPointRecordQuery;
import com.nexora.entity.vo.PointAccountVO;
import com.nexora.mappers.StudentPointAccountMapper;
import com.nexora.mappers.StudentPointRecordMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 学生积分查询编排（只读；写入统一走 {@link PointAwardComponent}，与掌握度组件的分工一致）。
 */
@Service
public class PointBiz {

    /** 明细列表单次最多返回条数（学生端展示最近记录，分页留到管理端流水页） */
    private static final int RECORD_LIMIT_DEFAULT = 20;
    private static final int RECORD_LIMIT_MAX = 100;

    @Resource
    private StudentPointAccountMapper accountMapper;

    @Resource
    private StudentPointRecordMapper recordMapper;

    @Resource
    private PointLevelComponent levelComponent;

    @Resource
    private PointAwardComponent pointAwardComponent;

    /** 我的积分账户概览（账户不存在时返回零值，等级按 1 级起算） */
    public PointAccountVO myAccount(String userId) {
        StudentPointAccount account = userId == null ? null : accountMapper.selectByUserId(userId);
        int total = account == null || account.getTotalPoints() == null ? 0 : account.getTotalPoints();
        PointAccountVO vo = new PointAccountVO();
        vo.setTotalPoints(total);
        vo.setAvailablePoints(account == null || account.getAvailablePoints() == null ? 0 : account.getAvailablePoints());
        int level = levelComponent.levelOf(total);
        vo.setLevel(level);
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
}
