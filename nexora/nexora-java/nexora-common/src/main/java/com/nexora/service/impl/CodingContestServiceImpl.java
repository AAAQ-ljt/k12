package com.nexora.service.impl;

import com.nexora.entity.enums.PageSize;
import com.nexora.entity.po.CodingContest;
import com.nexora.entity.po.CodingContestProblem;
import com.nexora.entity.po.CodingContestRecord;
import com.nexora.entity.query.CodingContestProblemQuery;
import com.nexora.entity.query.CodingContestQuery;
import com.nexora.entity.query.CodingContestRecordQuery;
import com.nexora.entity.query.SimplePage;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.mappers.CodingContestMapper;
import com.nexora.mappers.CodingContestProblemMapper;
import com.nexora.mappers.CodingContestRecordMapper;
import com.nexora.service.CodingContestService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 编程比赛 业务接口实现（比赛 + 赛题编排 + 报名/成绩，三张表在同一领域内聚合）
 */
@Service("codingContestService")
public class CodingContestServiceImpl implements CodingContestService {

    @Resource
    private CodingContestMapper<CodingContest, CodingContestQuery> codingContestMapper;

    @Resource
    private CodingContestProblemMapper<CodingContestProblem, CodingContestProblemQuery> codingContestProblemMapper;

    @Resource
    private CodingContestRecordMapper<CodingContestRecord, CodingContestRecordQuery> codingContestRecordMapper;

    @Override
    public List<CodingContest> findListByParam(CodingContestQuery param) {
        return codingContestMapper.selectList(param);
    }

    @Override
    public Integer findCountByParam(CodingContestQuery param) {
        return codingContestMapper.selectCount(param);
    }

    @Override
    public PaginationResultVO<CodingContest> findListByPage(CodingContestQuery param) {
        int count = findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();
        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        return new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(),
                findListByParam(param));
    }

    @Override
    public CodingContest getCodingContestByContestId(String contestId) {
        return codingContestMapper.selectByContestId(contestId);
    }

    @Override
    public Integer add(CodingContest bean) {
        return codingContestMapper.insert(bean);
    }

    @Override
    public Integer updateCodingContestByContestId(CodingContest bean, String contestId) {
        return codingContestMapper.updateByContestId(bean, contestId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer deleteCodingContestByContestId(String contestId) {
        codingContestProblemMapper.deleteByContestId(contestId);
        return codingContestMapper.deleteByContestId(contestId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveContestProblems(String contestId, List<String> problemIds, List<Integer> scoreOverrides) {
        codingContestProblemMapper.deleteByContestId(contestId);
        if (problemIds == null || problemIds.isEmpty()) {
            return;
        }
        Date now = new Date();
        List<CodingContestProblem> list = new ArrayList<>();
        for (int i = 0; i < problemIds.size(); i++) {
            CodingContestProblem item = new CodingContestProblem();
            item.setContestId(contestId);
            item.setProblemId(problemIds.get(i));
            item.setSort(i + 1);
            if (scoreOverrides != null && i < scoreOverrides.size()) {
                item.setScoreOverride(scoreOverrides.get(i));
            }
            item.setCreateTime(now);
            list.add(item);
        }
        codingContestProblemMapper.insertBatch(list);
    }

    @Override
    public List<CodingContestProblem> findProblemsByContestId(String contestId) {
        CodingContestProblemQuery query = new CodingContestProblemQuery();
        query.setContestId(contestId);
        return codingContestProblemMapper.selectList(query);
    }

    @Override
    public List<CodingContestProblem> findProblemsByContestIds(List<String> contestIds) {
        if (contestIds == null || contestIds.isEmpty()) {
            return new ArrayList<>();
        }
        CodingContestProblemQuery query = new CodingContestProblemQuery();
        query.setContestIds(contestIds);
        return codingContestProblemMapper.selectList(query);
    }

    @Override
    public List<CodingContestRecord> findRecordsByParam(CodingContestRecordQuery param) {
        return codingContestRecordMapper.selectList(param);
    }

    @Override
    public Integer countRecordsByParam(CodingContestRecordQuery param) {
        return codingContestRecordMapper.selectCount(param);
    }

    @Override
    public CodingContestRecord getRecord(String contestId, String userId) {
        return codingContestRecordMapper.selectByContestIdAndUserId(contestId, userId);
    }

    @Override
    public Integer addRecord(CodingContestRecord record) {
        return codingContestRecordMapper.insert(record);
    }

    @Override
    public Integer updateRecord(CodingContestRecord record, String contestId, String userId) {
        return codingContestRecordMapper.updateByContestIdAndUserId(record, contestId, userId);
    }
}
