package com.nexora.service;

import com.nexora.component.PointAwardComponent;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
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

    private static final Logger log = LoggerFactory.getLogger(CodingLabBiz.class);

    @Resource
    private CodingProblemService codingProblemService;

    /**
     * 积分发放入口（编程题通关）：练习模式通关发分、比赛模式不发（比赛成绩只进比赛排行榜）。
     * 积分异常只记日志，绝不影响判分。
     */
    @Resource
    private PointAwardComponent pointAwardComponent;

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
     * 可见性口径与题目详情对齐：已下架、或不属于当前学段的题目都不下发答案
     * （原实现只判「题目存在」，跨学段/已下架也能取到参考答案，见 docs/二期规划设计 §5.1.5-A5）。
     *
     * @param contestId 非空表示在比赛模式下查看，按比赛规则校验
     * @param stage     当前登录学生的学段
     */
    public CodingProblemReferenceVO getReference(String problemId, String contestId, String userId, String stage) {
        CodingProblem problem = codingProblemService.getCodingProblemByProblemId(problemId);
        if (problem == null || problem.getStatus() == null || problem.getStatus() != 1) {
            throw new BusinessException("题目不存在或已下架");
        }
        if (!StringTools.isEmpty(stage) && !StringTools.isEmpty(problem.getStage())
                && !stage.equals(problem.getStage())) {
            throw new BusinessException("该题目不属于当前学段");
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
     * 公平性口径（2026-10-06 用户确认，见 docs/二期规划设计 §5.1.5）：
     * - 比对前统一**规范化**：换行统一、去掉每行行尾空白、去掉首尾空行（避免「行尾多一个空格 / \r\n /
     *   末尾多一个空行」把做对的学生判成没做对）；
     * - 关键词判定从「命中任一」收紧为「**全部命中** 且 输出非空」；
     * - 失败时给**具体诊断**：只差中文全角符号 / 空行数量不同 / 大小写不一致 / 空格数量不同 /
     *   数值写法不同（78.5 与 78.50）——格式类差异如实告知；内容类差异只指向题目的「输出要求」，
     *   不点名缺少哪个关键词（避免退化成「照着提示凑关键词」）；
     * - 题目开启 `numeric_tolerant` 时，两边都能解析为数字的行按数值比较。
     *
     * @param stage     当前登录学生的学段（与题目详情同口径，不允许跨学段判分）
     * @param contestId 非空表示比赛模式：题目必须属于这场比赛
     * @param userId    当前登录学生（用于通关积分；为空则不发分）
     * @return [0]=是否通过，[1]=给学生的提示
     */
    public Object[] judge(String problemId, String output, String stage, String contestId, String userId) {
        CodingProblem problem = codingProblemService.getCodingProblemByProblemId(problemId);
        if (problem == null || problem.getStatus() == null || problem.getStatus() != 1) {
            throw new BusinessException("题目不存在或已下架");
        }
        if (!StringTools.isEmpty(stage) && !StringTools.isEmpty(problem.getStage())
                && !stage.equals(problem.getStage())) {
            throw new BusinessException("该题目不属于当前学段");
        }
        if (!StringTools.isEmpty(contestId)) {
            requireContestProblem(contestId, problemId);
        }
        String actual = normalizeOutput(output);
        Integer judgeType = problem.getJudgeType() == null ? 1 : problem.getJudgeType();
        Object[] result = judgeType == 2 ? judgeExact(problem, actual)
                : judgeType == 3 ? judgePattern(problem, actual)
                : judgeKeywords(problem, actual);
        // 通关积分（二期规划 A-2 第 6/7 条）：
        // - **比赛模式不发全局积分**（比赛成绩只进比赛自己的排行榜，见 A-5 分离说明）；
        // - 练习模式：bizId=题目 ID 保证「重复通关不再计分」，看过答案按 30%（服务端 Redis 标记）；
        // - 积分异常只记日志：绝不能因为积分问题让学生判不了题。
        if (Boolean.TRUE.equals(result[0]) && !StringTools.isEmpty(userId) && StringTools.isEmpty(contestId)) {
            try {
                pointAwardComponent.awardCodingProblem(userId, stage, problemId,
                        problem.getScore() == null ? 0 : problem.getScore());
            } catch (Exception e) {
                log.warn("编程题通关积分发放失败（不影响判分）userId={} problemId={}", userId, problemId, e);
            }
        }
        return result;
    }

    /** 比赛模式：题目必须在这场比赛的赛题里（判分与「显示答案」都按此校验） */
    private void requireContestProblem(String contestId, String problemId) {
        List<CodingContestProblem> relations = codingContestService.findProblemsByContestId(contestId);
        boolean belongs = relations != null && relations.stream()
                .anyMatch(relation -> problemId.equals(relation.getProblemId()));
        if (!belongs) {
            throw new BusinessException("该题目不属于这场比赛");
        }
    }

    /** 关键词判定：**全部**关键词命中 且 输出非空 */
    private Object[] judgeKeywords(CodingProblem problem, String actual) {
        List<String> required = new ArrayList<>();
        if (!StringTools.isEmpty(problem.getExpectedKeywords())) {
            for (String keyword : problem.getExpectedKeywords().split(",")) {
                String value = keyword.trim();
                if (!value.isEmpty()) {
                    required.add(value);
                }
            }
        }
        if (required.isEmpty()) {
            return new Object[]{false, "本题判定还没配置好，先跳过这道题吧（可以告诉老师）。"};
        }
        if (StringTools.isEmpty(actual)) {
            return new Object[]{false, "程序没有任何输出：题目要求输出里包含的要点，需要真的打印出来才行。"};
        }
        boolean missing = required.stream().anyMatch(keyword -> !actual.contains(keyword));
        if (missing) {
            return new Object[]{false, "输出里还缺少题目要求的内容：对照本题的「输出要求 / 输出示例」逐项检查一下。"};
        }
        return new Object[]{true, "输出包含本题要求的全部内容，通过！"};
    }

    /** 精确匹配判定：规范化后逐字比对（题目开启数值容差时，纯数字行按数值比较） */
    private Object[] judgeExact(CodingProblem problem, String actual) {
        String expected = normalizeOutput(problem.getExpectedOutput());
        if (StringTools.isEmpty(expected)) {
            return new Object[]{false, "本题判定还没配置好，先跳过这道题吧（可以告诉老师）。"};
        }
        if (StringTools.isEmpty(actual)) {
            return new Object[]{false, "程序没有任何输出：对照本题的「输出要求」，让程序把结果打印出来。"};
        }
        if (expected.equals(actual)) {
            return new Object[]{true, "输出与题目要求完全一致，通过！"};
        }
        boolean numericTolerant = problem.getNumericTolerant() != null && problem.getNumericTolerant() == 1;
        if (numericTolerant && sameAsNumbers(expected, actual)) {
            return new Object[]{true, "输出内容正确（数值写法不同，已按数值比较），通过！"};
        }
        return new Object[]{false, diagnoseExact(expected, actual)};
    }

    /** 正则判定：题目要求的输出格式匹配（正则非法时兜底提示，不把异常抛给学生） */
    private Object[] judgePattern(CodingProblem problem, String actual) {
        if (StringTools.isEmpty(problem.getExpectedPattern())) {
            return new Object[]{false, "本题判定还没配置好，先跳过这道题吧（可以告诉老师）。"};
        }
        if (StringTools.isEmpty(actual)) {
            return new Object[]{false, "程序没有任何输出：对照本题的「输出要求」再试一次。"};
        }
        boolean passed;
        try {
            passed = Pattern.compile(problem.getExpectedPattern(), Pattern.DOTALL).matcher(actual).find();
        } catch (PatternSyntaxException e) {
            log.warn("题目 {} 的期望正则非法，判分已跳过正则校验：{}", problem.getProblemId(), problem.getExpectedPattern());
            return new Object[]{false, "本题判定还没配置好，先跳过这道题吧（可以告诉老师）。"};
        }
        return new Object[]{passed, passed ? "输出符合题目要求的格式，通过！"
                : "输出的内容和格式还不符合要求：对照本题的「输出要求 / 输出示例」再检查一下。"};
    }

    /**
     * 精确匹配失败时的诊断：先判断是不是「内容对了但格式不同」，是就如实说清差在哪。
     * 只做诊断、不参与判定，所以怎么宽松都不会把错的判成对的。
     */
    private String diagnoseExact(String expected, String actual) {
        String expectedBase = stripBlankLines(expected);
        String actualBase = stripBlankLines(actual);
        if (expectedBase.equals(actualBase)) {
            return "输出内容是对的，只是空行数量不一样——按「输出要求」里的行数再核对一下换行。";
        }
        if (toHalfWidth(expectedBase).equals(toHalfWidth(actualBase))) {
            return "输出内容是对的，只是标点用了中文全角符号（如 ，：（）＝）——把输入法切到英文、改成半角符号就能通过。";
        }
        if (expectedBase.equalsIgnoreCase(actualBase)) {
            return "输出内容是对的，只是英文大小写不一致——按「输出要求」里的大小写再试一次。";
        }
        if (collapseSpaces(expected).equals(collapseSpaces(actual))) {
            return "输出内容是对的，只是空格数量不一样——按「输出示例」逐行核对空格（尤其是每行中间的空格）。";
        }
        if (sameAsNumbers(expected, actual)) {
            return "输出内容是对的，只是数值写法不同（例如 78.5 与 78.50）——按「输出要求」的小数位数输出即可。";
        }
        return "输出与题目要求还不一致：对照本题的「输出要求 / 输出示例」逐行检查（内容、顺序、标点与空格）。";
    }

    /**
     * 输出规范化（判分统一口径）：统一换行符 → 去掉每行行尾空白 → 去掉首尾空行。
     * 行内空格与行数保持不变（格式化输出类题目仍能考「空格与排版」）。
     */
    private String normalizeOutput(String text) {
        if (text == null) {
            return "";
        }
        String[] lines = text.replace("\r\n", "\n").replace("\r", "\n").split("\n", -1);
        List<String> normalized = new ArrayList<>(lines.length);
        for (String line : lines) {
            normalized.add(stripTrailingBlank(line));
        }
        int start = 0;
        int end = normalized.size();
        while (start < end && normalized.get(start).isEmpty()) {
            start++;
        }
        while (end > start && normalized.get(end - 1).isEmpty()) {
            end--;
        }
        return String.join("\n", normalized.subList(start, end));
    }

    private String stripTrailingBlank(String line) {
        int end = line.length();
        while (end > 0 && Character.isWhitespace(line.charAt(end - 1))) {
            end--;
        }
        return line.substring(0, end);
    }

    /** 去掉空行（保留行内内容），用于诊断「只差空行」 */
    private String stripBlankLines(String text) {
        return Arrays.stream(text.split("\n", -1))
                .filter(line -> !line.trim().isEmpty())
                .collect(Collectors.joining("\n"));
    }

    /** 去掉空行 + 行首尾空白 + 行内连续空白合并为一个空格，用于诊断「只差空格」 */
    private String collapseSpaces(String text) {
        return Arrays.stream(text.split("\n", -1))
                .map(line -> line.trim().replaceAll("[ \t]+", " "))
                .filter(line -> !line.isEmpty())
                .collect(Collectors.joining("\n"));
    }

    /** 全角标点/字母数字/全角空格 → 半角（仅用于诊断，不参与判定） */
    private String toHalfWidth(String text) {
        StringBuilder builder = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            if (c == '\u3000') {
                builder.append(' ');
            } else if (c >= '\uFF01' && c <= '\uFF5E') {
                builder.append((char) (c - 0xFEE0));
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    /** 数值行容差比较：行数一致，且每行「原文相等」或「两边都能解析为数字且数值相等（1e-6）」 */
    private boolean sameAsNumbers(String expected, String actual) {
        String[] expectedLines = expected.split("\n", -1);
        String[] actualLines = actual.split("\n", -1);
        if (expectedLines.length != actualLines.length) {
            return false;
        }
        for (int i = 0; i < expectedLines.length; i++) {
            String left = expectedLines[i].trim();
            String right = actualLines[i].trim();
            if (left.equals(right)) {
                continue;
            }
            Double leftValue = parseNumber(left);
            Double rightValue = parseNumber(right);
            if (leftValue == null || rightValue == null) {
                return false;
            }
            if (Math.abs(leftValue - rightValue) > 1e-6) {
                return false;
            }
        }
        return true;
    }

    /** 整行就是一个数字才参与容差比较，其余（含文本行）一律不认 */
    private Double parseNumber(String text) {
        if (StringTools.isEmpty(text) || text.length() > 24) {
            return null;
        }
        try {
            return Double.valueOf(text);
        } catch (NumberFormatException e) {
            return null;
        }
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
        // 输出契约：输出要求 + 输出示例随题目下发（示例用另一组数据演示格式，不含本题答案）；
        // judgeType 只用于前端讲清「怎么算通过」，预期输出/关键词仍不下发
        vo.setOutputSpec(problem.getOutputSpec());
        vo.setOutputExample(problem.getOutputExample());
        vo.setNumericTolerant(problem.getNumericTolerant());
        vo.setJudgeType(problem.getJudgeType());
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
