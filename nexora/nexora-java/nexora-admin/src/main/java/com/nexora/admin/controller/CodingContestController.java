package com.nexora.admin.controller;

import com.nexora.constants.Constants;
import com.nexora.controller.ABaseController;
import com.nexora.entity.po.CodingContest;
import com.nexora.entity.po.CodingContestProblem;
import com.nexora.entity.po.CodingProblem;
import com.nexora.entity.query.CodingContestQuery;
import com.nexora.entity.query.CodingProblemQuery;
import com.nexora.entity.query.CodingContestRecordQuery;
import com.nexora.entity.po.CodingContestRecord;
import com.nexora.entity.vo.CodingContestVO;
import com.nexora.entity.vo.CodingProblemVO;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.vo.ResponseVO;
import com.nexora.exception.BusinessException;
import com.nexora.service.CodingContestService;
import com.nexora.service.CodingProblemService;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理端「编程题库」比赛管理（发布 / 赛题编排 / 参赛情况）。
 *
 * 注意：一期赛题不下发参考答案给管理端列表页，详情页会带（管理员需要看答案做审核）。
 */
@RestController
@RequestMapping("/codingContest")
public class CodingContestController extends ABaseController {

    @Resource
    private CodingContestService codingContestService;

    @Resource
    private CodingProblemService codingProblemService;

    @GetMapping("/loadDataList")
    public ResponseVO<PaginationResultVO<CodingContestVO>> loadDataList(CodingContestQuery query) {
        if (query.getPageNo() == null) {
            query.setPageNo(1);
        }
        if (query.getPageSize() == null) {
            query.setPageSize(15);
        }
        PaginationResultVO<CodingContest> page = codingContestService.findListByPage(query);
        List<CodingContest> contests = page.getList();
        List<CodingContestVO> list = new ArrayList<>();
        if (contests != null && !contests.isEmpty()) {
            List<String> contestIds = contests.stream().map(CodingContest::getContestId).toList();
            // 一次取回全部赛题关系（禁止循环查库），再按比赛分组
            List<CodingContestProblem> relations = codingContestService.findProblemsByContestIds(contestIds);
            Map<String, List<CodingContestProblem>> relationMap = new HashMap<>();
            for (CodingContestProblem relation : relations) {
                relationMap.computeIfAbsent(relation.getContestId(), key -> new ArrayList<>()).add(relation);
            }
            // 一次取回相关题目（拿积分），避免 N+1
            List<String> problemIds = relations.stream().map(CodingContestProblem::getProblemId).distinct().toList();
            Map<String, Integer> problemScoreMap = new HashMap<>();
            if (!problemIds.isEmpty()) {
                CodingProblemQuery problemQuery = new CodingProblemQuery();
                problemQuery.setProblemIds(problemIds);
                for (CodingProblem problem : codingProblemService.findListByParam(problemQuery)) {
                    problemScoreMap.put(problem.getProblemId(), problem.getScore());
                }
            }
            // 参赛人数（按比赛分组统计）
            Map<String, Integer> submitCountMap = new HashMap<>();
            for (String contestId : contestIds) {
                CodingContestRecordQuery recordQuery = new CodingContestRecordQuery();
                recordQuery.setContestId(contestId);
                recordQuery.setStatus(2);
                submitCountMap.put(contestId, codingContestService.countRecordsByParam(recordQuery));
            }
            for (CodingContest contest : contests) {
                CodingContestVO vo = toVO(contest);
                List<CodingContestProblem> items = relationMap.getOrDefault(contest.getContestId(), new ArrayList<>());
                vo.setProblemCount(items.size());
                int totalScore = 0;
                for (CodingContestProblem item : items) {
                    totalScore += item.getScoreOverride() != null
                            ? item.getScoreOverride()
                            : problemScoreMap.getOrDefault(item.getProblemId(), 0);
                }
                vo.setTotalScore(totalScore);
                vo.setSubmitCount(submitCountMap.get(contest.getContestId()));
                list.add(vo);
            }
        }
        return getSuccessResponseVO(new PaginationResultVO<>(page.getTotalCount(), page.getPageSize(),
                page.getPageNo(), page.getPageTotal(), list));
    }

    @GetMapping("/getInfo")
    public ResponseVO<CodingContestVO> getInfo(@RequestParam String contestId) {
        CodingContest contest = codingContestService.getCodingContestByContestId(contestId);
        if (contest == null) {
            throw new BusinessException("比赛不存在");
        }
        CodingContestVO vo = toVO(contest);
        List<CodingContestProblem> items = codingContestService.findProblemsByContestId(contestId);
        List<CodingProblemVO> problems = new ArrayList<>();
        int totalScore = 0;
        if (!items.isEmpty()) {
            List<String> problemIds = items.stream().map(CodingContestProblem::getProblemId).toList();
            CodingProblemQuery problemQuery = new CodingProblemQuery();
            problemQuery.setProblemIds(problemIds);
            Map<String, CodingProblem> problemMap = new HashMap<>();
            for (CodingProblem problem : codingProblemService.findListByParam(problemQuery)) {
                problemMap.put(problem.getProblemId(), problem);
            }
            for (CodingContestProblem item : items) {
                CodingProblem problem = problemMap.get(item.getProblemId());
                if (problem == null) {
                    continue;
                }
                CodingProblemVO problemVO = new CodingProblemVO();
                problemVO.setProblemId(problem.getProblemId());
                problemVO.setStage(problem.getStage());
                problemVO.setDifficulty(problem.getDifficulty());
                problemVO.setTitle(problem.getTitle());
                problemVO.setGoal(problem.getGoal());
                problemVO.setScore(item.getScoreOverride() != null ? item.getScoreOverride() : problem.getScore());
                problemVO.setSort(item.getSort());
                problems.add(problemVO);
                totalScore += problemVO.getScore() == null ? 0 : problemVO.getScore();
            }
        }
        vo.setProblems(problems);
        vo.setProblemCount(problems.size());
        vo.setTotalScore(totalScore);
        return getSuccessResponseVO(vo);
    }

    @PostMapping("/add")
    public ResponseVO<String> add(@RequestBody CodingContest bean) {
        validate(bean);
        bean.setContestId(StringTools.getRandomNumber(Constants.LENGTH_15));
        if (bean.getStatus() == null) {
            bean.setStatus(0);
        }
        if (bean.getAllowAnswer() == null) {
            bean.setAllowAnswer(0);
        }
        bean.setCreateTime(new Date());
        bean.setUpdateTime(new Date());
        codingContestService.add(bean);
        return getSuccessResponseVO(bean.getContestId());
    }

    @PutMapping("/update")
    public ResponseVO<Void> update(@RequestBody CodingContest bean) {
        if (StringTools.isEmpty(bean.getContestId())) {
            throw new BusinessException("比赛ID不能为空");
        }
        validate(bean);
        bean.setUpdateTime(new Date());
        codingContestService.updateCodingContestByContestId(bean, bean.getContestId());
        return getSuccessResponseVO(null);
    }

    /** 发布（草稿 → 已发布） */
    @PutMapping("/publish")
    public ResponseVO<Void> publish(@RequestParam String contestId) {
        changeStatus(contestId, 1);
        return getSuccessResponseVO(null);
    }

    /** 提前结束 */
    @PutMapping("/finish")
    public ResponseVO<Void> finish(@RequestParam String contestId) {
        changeStatus(contestId, 2);
        return getSuccessResponseVO(null);
    }

    @DeleteMapping("/del")
    public ResponseVO<Void> del(@RequestParam String contestId) {
        if (StringTools.isEmpty(contestId)) {
            throw new BusinessException("比赛ID不能为空");
        }
        codingContestService.deleteCodingContestByContestId(contestId);
        return getSuccessResponseVO(null);
    }

    /** 赛题编排：按传入顺序保存（可选逐题覆盖分值） */
    @PostMapping("/saveProblems")
    public ResponseVO<Void> saveProblems(@RequestParam String contestId,
                                         @RequestBody List<Map<String, Object>> problems) {
        if (StringTools.isEmpty(contestId)) {
            throw new BusinessException("比赛ID不能为空");
        }
        List<String> problemIds = new ArrayList<>();
        List<Integer> scoreOverrides = new ArrayList<>();
        if (problems != null) {
            for (Map<String, Object> item : problems) {
                Object problemId = item.get("problemId");
                if (problemId == null || StringTools.isEmpty(String.valueOf(problemId))) {
                    continue;
                }
                problemIds.add(String.valueOf(problemId));
                Object score = item.get("score");
                // 前端可能传 20 或 20.0，统一按数值解析后取整
                scoreOverrides.add(score == null ? null
                        : new java.math.BigDecimal(String.valueOf(score)).intValue());
            }
        }
        if (problemIds.isEmpty()) {
            throw new BusinessException("请至少选择一道赛题");
        }
        codingContestService.saveContestProblems(contestId, problemIds, scoreOverrides);
        return getSuccessResponseVO(null);
    }

    /** 参赛情况（谁报名/得分，一期仅列出，导出与排行榜详情二期） */
    @GetMapping("/recordList")
    public ResponseVO<List<CodingContestRecord>> recordList(@RequestParam String contestId) {
        CodingContestRecordQuery query = new CodingContestRecordQuery();
        query.setContestId(contestId);
        query.setOrderByScoreDesc(Boolean.TRUE);
        return getSuccessResponseVO(codingContestService.findRecordsByParam(query));
    }

    private void changeStatus(String contestId, int status) {
        CodingContest contest = codingContestService.getCodingContestByContestId(contestId);
        if (contest == null) {
            throw new BusinessException("比赛不存在");
        }
        if (status == 1 && contest.getEndTime() != null && contest.getEndTime().before(new Date())) {
            throw new BusinessException("结束时间已过，无法发布");
        }
        CodingContest update = new CodingContest();
        update.setStatus(status);
        update.setUpdateTime(new Date());
        codingContestService.updateCodingContestByContestId(update, contestId);
    }

    private void validate(CodingContest bean) {
        if (StringTools.isEmpty(bean.getTitle())) {
            throw new BusinessException("比赛名称不能为空");
        }
        if (StringTools.isEmpty(bean.getStage())) {
            throw new BusinessException("请选择面向学段");
        }
        if (bean.getStartTime() == null || bean.getEndTime() == null) {
            throw new BusinessException("请设置比赛开始与结束时间");
        }
        if (!bean.getEndTime().after(bean.getStartTime())) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }
    }

    private CodingContestVO toVO(CodingContest contest) {
        CodingContestVO vo = new CodingContestVO();
        vo.setContestId(contest.getContestId());
        vo.setTitle(contest.getTitle());
        vo.setStage(contest.getStage());
        vo.setDescription(contest.getDescription());
        vo.setStartTime(contest.getStartTime());
        vo.setEndTime(contest.getEndTime());
        vo.setDurationMinutes(contest.getDurationMinutes());
        vo.setAllowAnswer(contest.getAllowAnswer());
        vo.setStatus(contest.getStatus());
        return vo;
    }
}
