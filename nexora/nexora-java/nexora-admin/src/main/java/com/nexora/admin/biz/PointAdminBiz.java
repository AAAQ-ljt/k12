package com.nexora.admin.biz;

import com.nexora.component.PointAwardComponent;
import com.nexora.entity.po.StudentPointAccount;
import com.nexora.entity.po.StudentPointRecord;
import com.nexora.entity.query.StudentPointRecordQuery;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.vo.PointRankItemVO;
import com.nexora.exception.BusinessException;
import com.nexora.mappers.StudentPointAccountMapper;
import com.nexora.mappers.StudentPointRecordMapper;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 管理端积分运营 Biz（二期 A-8）。
 *
 * 只做「看账 + 人工补分」两件事：
 * 1) 流水审计：按学生/来源/时间查流水（积分全部由学习事件在服务端结算，管理端改不了判分逻辑）；
 * 2) 人工补分：**走 {@link PointAwardComponent} 这个唯一发分入口**（不绕过幂等与流水），
 *    且只允许正数——扣分属于兑换体系（A-7）的职责，避免运营误操作破坏「累计积分只增不减」的口径。
 */
@Slf4j
@Component
public class PointAdminBiz {

    /** 人工补分单次上限（防误输入把榜刷爆） */
    private static final int ADJUST_MAX = 1000;

    /** 人工补分幂等键前缀：`ADJUST-<操作人>-<时间戳>`，天然一次一个事件 */
    private static final String BIZ_ADJUST = "ADJUST";

    /** 审计列表最大返回条数（用筛选缩小范围，不做深翻页） */
    private static final int AUDIT_LIMIT_MAX = 200;

    /** 学生积分总览条数上限 */
    private static final int ACCOUNT_LIMIT = 200;

    @Resource
    private StudentPointRecordMapper recordMapper;

    @Resource
    private StudentPointAccountMapper accountMapper;

    @Resource
    private PointAwardComponent pointAwardComponent;

    /**
     * 流水审计（时间倒序，最多 AUDIT_LIMIT_MAX 条；totalCount 是命中筛选的总条数，用来提示还有多少）。
     */
    public PaginationResultVO<StudentPointRecord> loadDataList(StudentPointRecordQuery query) {
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1
                ? 50 : Math.min(query.getPageSize(), AUDIT_LIMIT_MAX);
        query.setPageSize(pageSize);
        Integer totalCount = recordMapper.selectCountByParam(query);
        List<StudentPointRecord> list = recordMapper.selectListByParam(query);
        PaginationResultVO<StudentPointRecord> result = new PaginationResultVO<>();
        result.setPageNo(1);
        result.setPageSize(pageSize);
        result.setTotalCount(totalCount == null ? 0 : totalCount);
        result.setPageTotal(list == null ? 0 : list.size());
        result.setList(list == null ? List.of() : list);
        return result;
    }

    /**
     * 学生积分总览（按累计积分倒序，含昵称/学段/等级；管理端可见身份，脱敏只在学生端做）。
     */
    public List<PointRankItemVO> accountList() {
        return accountMapper.selectRankRows(null, null, ACCOUNT_LIMIT);
    }

    /**
     * 人工补分（补偿/活动奖励）。返回本次实际发放的积分：0 表示被幂等或每日上限拦下。
     */
    public int addPoints(String userId, String stage, Integer points, String reason, String operatorId) {
        if (StringTools.isEmpty(userId)) {
            throw new BusinessException("请先指定学生");
        }
        if (points == null || points <= 0) {
            throw new BusinessException("补分必须是大于 0 的整数");
        }
        if (points > ADJUST_MAX) {
            throw new BusinessException("单次补分不能超过 " + ADJUST_MAX + " 分");
        }
        if (StringTools.isEmpty(reason)) {
            throw new BusinessException("请填写补分原因（会记入流水，学生端可见）");
        }
        // 学段优先用页面传的（学生还没有积分账户时也能记对学段），否则回落到账户快照
        String effectiveStage = stage;
        if (StringTools.isEmpty(effectiveStage)) {
            StudentPointAccount account = accountMapper.selectByUserId(userId);
            effectiveStage = account == null ? null : account.getStage();
        }
        String bizId = BIZ_ADJUST + "-" + (StringTools.isEmpty(operatorId) ? "admin" : operatorId)
                + "-" + System.currentTimeMillis();
        int granted = pointAwardComponent.award(userId, effectiveStage, BIZ_ADJUST, bizId, points,
                "管理员补分：" + reason.trim());
        log.info("管理端人工补分 operator={} userId={} points={} granted={} reason={}",
                operatorId, userId, points, granted, reason);
        return granted;
    }
}
