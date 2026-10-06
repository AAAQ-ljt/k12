package com.nexora.service;

import com.nexora.entity.po.CodingContest;
import com.nexora.entity.po.CodingContestProblem;
import com.nexora.entity.po.CodingContestRecord;
import com.nexora.entity.po.CodingProblem;
import com.nexora.entity.query.CodingContestQuery;
import com.nexora.entity.query.CodingContestRecordQuery;
import com.nexora.entity.query.CodingProblemQuery;
import com.nexora.entity.vo.CodingContestRecordVO;
import com.nexora.entity.vo.CodingContestVO;
import com.nexora.entity.vo.CodingProblemReferenceVO;
import com.nexora.entity.vo.CodingProblemVO;
import com.nexora.exception.BusinessException;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 编程实验室业务编排（学生端）：题库列表/详情、参考答案的可见性规则、比赛报名与成绩。
 *
 * 关键规则（2026-10-05 用户确认）：
 * - 参考答案**不随题目列表/详情下发**，必须单独走「显示答案」接口；
 * - 比赛进行中默认禁止看答案（`coding_contest.allow_answer=1` 才放行），提交后可看答案复盘；
 * - 练习模式看答案允许，但前端按 30% 计分（`scoreDiscounted=true`）。
 */
@Service
public class CodingLabBiz {

    @Resource
    private CodingProblemService codingProblemService;

    @Resource
    private CodingContestService codingContestService;

    /** 学生端题库列表（按难度由易到难） */
    public List<CodingProblemVO> listProblems(String stage, Integer difficulty, String keyword) {
        CodingProblemQuery query = new CodingProblemQuery();
        query.setStage(stage);
        query.setStatus(1);
        query.setDifficulty(difficulty);
        query.setKeywordFuzzy(keyword);
        query.setOrderByDifficulty(Boolean.TRUE);
        return codingProblemService.findListByParam(query).stream().map(this::toProblemVO).toList();
    }

    /** 题目详情（含预置代码与提示，不含答案） */
    public CodingProblemVO getProblem(String problemId, String stage) {
        CodingProblem problem = codingProblemService.getCodingProblemByProblemId(problemId);
        if (problem == null || problem.getStatus() == null || problem.getStatus() != 1) {
            throw new BusinessException("题目不存在或已下架");
        }
        if (!StringTools.isEmpty(stage) && !StringTools.isEmpty(problem.getStage())
                && !stage.equals(problem.getStage())) {
            throw new BusinessException("该题目不属于当前学段");
        }
        return toProblemVO(problem);
    }

    /**
     * 参考答案（「显示答案」按钮）。
     *
     * @param contestId 非空表示在比赛模式下查看，按比赛规则校验
     */
    public CodingProblemReferenceVO getReference(String problemId, String contestId, String userId) {
        CodingProblem problem = codingProblemService.getCodingProblemByProblemId(problemId);
        if (problem == null) {
            throw new BusinessException("题目不存在");
        }
        CodingProblemReferenceVO vo = new CodingProblemReferenceVO();
        vo.setProblemId(problemId);
        if (StringTools.isEmpty(contestId)) {
            vo.setReferenceCode(problem.getReferenceCode());
            vo.setSolutionNotes(problem.getSolutionNotes());
            vo.setScoreDiscounted(Boolean.TRUE);
            return vo;
        }
        CodingContest contest = codingContestService.getCodingContestByContestId(contestId);
        if (contest == null) {
            vo.setReferenceCode(problem.getReferenceCode());
            vo.setSolutionNotes(problem.getSolutionNotes());
            vo.setScoreDiscounted(Boolean.TRUE);
            return vo;
        }
        CodingContestRecord record = StringTools.isEmpty(userId)
                ? null : codingContestService.getRecord(contestId, userId);
        boolean submitted = record != null && record.getStatus() != null && record.getStatus() == 2;
        boolean ended = contest.getEndTime() != null && contest.getEndTime().before(new Date());
        boolean allowed = ended || submitted
                || (contest.getAllowAnswer() != null && contest.getAllowAnswer() == 1);
        if (!allowed) {
            vo.setDenyReason("比赛进行中不可查看答案，先自己试一试；提交后可以复盘看答案");
            vo.setScoreDiscounted(Boolean.FALSE);
            return vo;
        }
        vo.setReferenceCode(problem.getReferenceCode());
        vo.setSolutionNotes(problem.getSolutionNotes());
        // 比赛期间看答案不加分（成绩以提交时为准），练习模式按 30% 计分
        vo.setScoreDiscounted(Boolean.FALSE);
        return vo;
    }

    /**
     * 判分：按题目配置的判定方式比对运行输出（预期值不下发前端，避免被翻查）。
     *
     * @return [0]=是否通过，[1]=给学生的提示
     */
    public Object[] judge(String problemId, String output) {
        CodingProblem problem = codingProblemService.getCodingProblemByProblemId(problemId);
        if (problem == null) {
            throw new BusinessException("题目不存在");
        }
        String actual = output == null ? "" : output.trim();
        Integer judgeType = problem.getJudgeType() == null ? 1 : problem.getJudgeType();
        if (judgeType == 2) {
            String expected = problem.getExpectedOutput() == null ? "" : problem.getExpectedOutput().trim();
            boolean passed = !expected.isEmpty() && expected.equals(actual);
            return new Object[]{passed, passed ? "输出与预期完全一致，通过！"
                    : "输出和预期还不太一样：检查一下打印的内容、顺序与空格。"};
        }
        if (judgeType == 3) {
            String pattern = problem.getExpectedPattern();
            boolean passed = !StringTools.isEmpty(pattern)
                    && java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.DOTALL).matcher(actual).find();
            return new Object[]{passed, passed ? "输出符合要求，通过！" : "输出还不符合要求的格式，再看看提示。"};
        }
        String keywords = problem.getExpectedKeywords();
        boolean passed = false;
        if (!StringTools.isEmpty(keywords)) {
            for (String keyword : keywords.split(",")) {
                String value = keyword.trim();
                if (!value.isEmpty() && actual.contains(value)) {
                    passed = true;
                    break;
                }
            }
        }
        return new Object[]{passed, passed ? "命中本关要点，通过！" : "还没有出现本关要求的输出，再调整一下。"};
    }

    /** 本学段可参加的比赛（已发布且未结束，含未开始可报名） */
    public List<CodingContestVO> listContests(String stage, String userId) {
        CodingContestQuery query = new CodingContestQuery();
        query.setStage(stage);
        query.setOpenOnly(Boolean.TRUE);
        query.setOrderByStartTimeDesc(Boolean.TRUE);
        List<CodingContest> contests = codingContestService.findListByParam(query);
        return assembleContests(contests, userId, false);
    }

    /** 我报名/参加过的比赛（含已结束，个人中心用） */
    public List<CodingContestVO> listMyContests(String userId) {
        CodingContestRecordQuery recordQuery = new CodingContestRecordQuery();
        recordQuery.setUserId(userId);
        List<CodingContestRecord> records = codingContestService.findRecordsByParam(recordQuery);
        if (records.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> contestIds = records.stream().map(CodingContestRecord::getContestId).distinct().toList();
        CodingContestQuery query = new CodingContestQuery();
        query.setContestIds(contestIds);
        List<CodingContest> contests = codingContestService.findListByParam(query);
        return assembleContests(contests, userId, true);
    }

    /** 比赛详情（含赛题列表，不含答案） */
    public CodingContestVO getContestDetail(String contestId, String userId, String stage) {
        CodingContest contest = codingContestService.getCodingContestByContestId(contestId);
        if (contest == null || contest.getStatus() == null || contest.getStatus() == 0) {
            throw new BusinessException("比赛不存在或未发布");
        }
        if (!StringTools.isEmpty(stage) && !StringTools.isEmpty(contest.getStage())
                && !stage.equals(contest.getStage())) {
            throw new BusinessException("本场比赛面向其他学段，你可以在自己学段的比赛里挑战");
        }
        List<CodingContestVO> assembled = assembleContests(List.of(contest), userId, true);
        return assembled.isEmpty() ? null : assembled.get(0);
    }

    /** 报名 */
    public void enroll(String contestId, String userId, String stage) {
        CodingContest contest = codingContestService.getCodingContestByContestId(contestId);
        if (contest == null || contest.getStatus() == null || contest.getStatus() != 1) {
            throw new BusinessException("比赛不存在或未发布");
        }
        if (!StringTools.isEmpty(contest.getStage()) && !contest.getStage().equals(stage)) {
            throw new BusinessException("本场比赛面向其他学段，无法报名");
        }
        if (contest.getEndTime() != null && contest.getEndTime().before(new Date())) {
            throw new BusinessException("比赛已结束，无法报名");
        }
        CodingContestRecord exists = codingContestService.getRecord(contestId, userId);
        if (exists != null) {
            return;
        }
        CodingContestRecord record = new CodingContestRecord();
        record.setContestId(contestId);
        record.setUserId(userId);
        record.setStage(stage);
        record.setStatus(0);
        record.setEnrollTime(new Date());
        record.setScore(0);
        record.setSolvedCount(0);
        record.setTotalCount(codingContestService.findProblemsByContestId(contestId).size());
        record.setDuration(0);
        codingContestService.addRecord(record);
    }

    /** 进入比赛（自动补报名） */
    public CodingContestVO start(String contestId, String userId, String stage) {
        enroll(contestId, userId, stage);
        CodingContest contest = codingContestService.getCodingContestByContestId(contestId);
        Date now = new Date();
        if (contest.getStartTime() != null && contest.getStartTime().after(now)) {
            throw new BusinessException("比赛尚未开始，先做好准备吧");
        }
        if (contest.getEndTime() != null && contest.getEndTime().before(now)) {
            throw new BusinessException("比赛已结束");
        }
        CodingContestRecord record = codingContestService.getRecord(contestId, userId);
        if (record.getStatus() == null || record.getStatus() == 0) {
            CodingContestRecord update = new CodingContestRecord();
            update.setStatus(1);
            update.setStartedAt(now);
            codingContestService.updateRecord(update, contestId, userId);
        }
        return getContestDetail(contestId, userId, stage);
    }

    /** 提交比赛成绩（一期为客户端判定成绩，二期接服务端权威判分） */
    public void submit(String contestId, String userId, Integer score, Integer solvedCount, Integer duration) {
        CodingContestRecord record = codingContestService.getRecord(contestId, userId);
        if (record == null) {
            throw new BusinessException("请先报名并进入比赛");
        }
        if (record.getStatus() != null && record.getStatus() == 2) {
            throw new BusinessException("本场比赛已提交，不能重复提交");
        }
        CodingContestRecord update = new CodingContestRecord();
        update.setStatus(2);
        update.setSubmittedAt(new Date());
        update.setScore(score == null ? 0 : Math.max(0, score));
        update.setSolvedCount(solvedCount == null ? 0 : Math.max(0, solvedCount));
        update.setDuration(duration == null ? 0 : Math.max(0, duration));
        codingContestService.updateRecord(update, contestId, userId);
    }

    /** 排行榜（一期为客户端判定成绩，名次按得分降序、用时升序） */
    public List<CodingContestRecordVO> rank(String contestId, String userId, Integer limit) {
        CodingContestRecordQuery query = new CodingContestRecordQuery();
        query.setContestId(contestId);
        query.setStatus(2);
        query.setOrderByScoreDesc(Boolean.TRUE);
        query.setPageNo(1);
        query.setPageSize(limit == null || limit <= 0 ? 50 : limit);
        List<CodingContestRecord> records = codingContestService.findRecordsByParam(query);
        List<CodingContestRecordVO> result = new ArrayList<>();
        int index = 0;
        for (CodingContestRecord record : records) {
            index++;
            result.add(toRecordVO(record, index, userId));
        }
        return result;
    }

    // ===== 内部工具 =====

    private List<CodingContestVO> assembleContests(List<CodingContest> contests, String userId, boolean withProblems) {
        List<CodingContestVO> result = new ArrayList<>();
        if (contests == null || contests.isEmpty()) {
            return result;
        }
        List<String> contestIds = contests.stream().map(CodingContest::getContestId).toList();
        Map<String, List<CodingContestProblem>> relationMap = new HashMap<>();
        for (CodingContestProblem relation : codingContestService.findProblemsByContestIds(contestIds)) {
            relationMap.computeIfAbsent(relation.getContestId(), key -> new ArrayList<>()).add(relation);
        }
        List<String> problemIds = relationMap.values().stream().flatMap(List::stream)
                .map(CodingContestProblem::getProblemId).distinct().toList();
        Map<String, CodingProblem> problemMap = new HashMap<>();
        if (!problemIds.isEmpty()) {
            CodingProblemQuery problemQuery = new CodingProblemQuery();
            problemQuery.setProblemIds(problemIds);
            for (CodingProblem problem : codingProblemService.findListByParam(problemQuery)) {
                problemMap.put(problem.getProblemId(), problem);
            }
        }
        // 我的报名/成绩（一次取回，避免循环查库）
        Map<String, CodingContestRecord> myRecordMap = new HashMap<>();
        if (!StringTools.isEmpty(userId)) {
            CodingContestRecordQuery recordQuery = new CodingContestRecordQuery();
            recordQuery.setUserId(userId);
            recordQuery.setContestIds(contestIds);
            for (CodingContestRecord record : codingContestService.findRecordsByParam(recordQuery)) {
                myRecordMap.put(record.getContestId(), record);
            }
        }
        // 已提交人数（一次取回后分组）
        CodingContestRecordQuery submittedQuery = new CodingContestRecordQuery();
        submittedQuery.setContestIds(contestIds);
        submittedQuery.setStatus(2);
        Map<String, Integer> submitCountMap = new HashMap<>();
        for (CodingContestRecord record : codingContestService.findRecordsByParam(submittedQuery)) {
            submitCountMap.merge(record.getContestId(), 1, Integer::sum);
        }
        for (CodingContest contest : contests) {
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
            List<CodingContestProblem> relations = relationMap.getOrDefault(contest.getContestId(), new ArrayList<>());
            vo.setProblemCount(relations.size());
            int totalScore = 0;
            List<CodingProblemVO> problems = new ArrayList<>();
            for (CodingContestProblem relation : relations) {
                CodingProblem problem = problemMap.get(relation.getProblemId());
                if (problem == null) {
                    continue;
                }
                int score = relation.getScoreOverride() != null ? relation.getScoreOverride()
                        : (problem.getScore() == null ? 0 : problem.getScore());
                totalScore += score;
                if (withProblems) {
                    CodingProblemVO problemVO = toProblemVO(problem);
                    problemVO.setScore(score);
                    problemVO.setSort(relation.getSort());
                    problems.add(problemVO);
                }
            }
            if (withProblems) {
                problems.sort(Comparator.comparing(item -> item.getSort() == null ? 0 : item.getSort()));
                vo.setProblems(problems);
            }
            vo.setTotalScore(totalScore);
            CodingContestRecord myRecord = myRecordMap.get(contest.getContestId());
            vo.setMyStatus(myRecord == null ? 0 : (myRecord.getStatus() == null ? 0 : myRecord.getStatus() + 1));
            vo.setMyScore(myRecord == null ? null : myRecord.getScore());
            vo.setMySolvedCount(myRecord == null ? null : myRecord.getSolvedCount());
            vo.setMyDuration(myRecord == null ? null : myRecord.getDuration());
            vo.setSubmitCount(submitCountMap.getOrDefault(contest.getContestId(), 0));
            result.add(vo);
        }
        return result;
    }

    private CodingProblemVO toProblemVO(CodingProblem problem) {
        CodingProblemVO vo = new CodingProblemVO();
        vo.setProblemId(problem.getProblemId());
        vo.setStage(problem.getStage());
        vo.setDifficulty(problem.getDifficulty());
        vo.setTitle(problem.getTitle());
        vo.setGoal(problem.getGoal());
        vo.setDescription(problem.getDescription());
        vo.setHint(problem.getHint());
        vo.setStarterCode(problem.getStarterCode());
        vo.setScore(problem.getScore());
        vo.setEstimateMinutes(problem.getEstimateMinutes());
        vo.setSort(problem.getSort());
        vo.setLanguage(problem.getLanguage());
        vo.setKnowledgePointId(problem.getKnowledgePointId());
        return vo;
    }

    private CodingContestRecordVO toRecordVO(CodingContestRecord record, int rank, String userId) {
        CodingContestRecordVO vo = new CodingContestRecordVO();
        vo.setRecordId(record.getRecordId());
        vo.setContestId(record.getContestId());
        vo.setUserId(record.getUserId());
        vo.setStage(record.getStage());
        vo.setStatus(record.getStatus());
        vo.setEnrollTime(record.getEnrollTime());
        vo.setSubmittedAt(record.getSubmittedAt());
        vo.setScore(record.getScore());
        vo.setSolvedCount(record.getSolvedCount());
        vo.setTotalCount(record.getTotalCount());
        vo.setDuration(record.getDuration());
        vo.setRank(rank);
        return vo;
    }

    /** 供控制器判断是否为我：返回脱敏后的名次标识（二期补昵称） */
    public List<CodingContestRecordVO> markMine(List<CodingContestRecordVO> list, String userId) {
        return list.stream().peek(item -> item.setUserId(
                item.getUserId() != null && item.getUserId().equals(userId) ? "me" : maskUserId(item.getUserId())
        )).collect(Collectors.toList());
    }

    private String maskUserId(String userId) {
        if (StringTools.isEmpty(userId) || userId.length() <= 4) {
            return "同学";
        }
        return "同学" + userId.substring(userId.length() - 4);
    }
}
