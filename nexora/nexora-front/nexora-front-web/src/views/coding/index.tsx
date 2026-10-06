import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { App, Button, Space, Tag, Tooltip } from 'antd';
import type { editor as MonacoEditorNs } from 'monaco-editor';
import Editor from '@monaco-editor/react';
import {
  Download,
  Eye,
  EyeOff,
  Flag,
  LogOut,
  Play,
  RefreshCw,
  RotateCcw,
  Save,
  Star,
  Terminal,
  Timer,
  Trophy,
} from 'lucide-react';
import {
  judgeCodingProblem,
  loadCodingContestInfo,
  loadCodingContests,
  loadCodingProblems,
  loadCodingReference,
  enrollCodingContest,
  startCodingContest,
  submitCodingContest,
  type CodingContestVO,
  type CodingProblemVO,
} from '@/api/codingLab';
import { prepareStudentUpload, uploadStudentShard } from '@/api/studentResource';
import { useAuthStore } from '@/stores/auth';
import { getStageOption } from '@/types/common';
import { contestDeadlineOf, contestPhaseOf, formatCountdown, formatDuration, parseTime } from '@/utils/coding';
import AiCoachPanel from './components/AiCoachPanel';
import ProblemPanel, { type CodingTabKey, type ContestSessionView } from './components/ProblemPanel';
import RunConsole from './components/RunConsole';
import { usePyodide } from './hooks/usePyodide';
import { MONACO_LIGHT, defineCodingThemes } from './monacoTheme';
import styles from './index.module.scss';

const PYTHON_TYPE = 'python';

/** 练习进度与草稿的本地存储键（按学段 / 题目区分） */
const passedKey = (stage: string) => `nexora:code:passed:${stage}`;
const scoreKey = (stage: string) => `nexora:code:score:${stage}`;
const answerUsedKey = (stage: string) => `nexora:code:answerUsed:${stage}`;
const draftKey = (stage: string, problemId: string) => `nexora:code:draft:${stage}:${problemId}`;
/** 比赛过程键（按比赛区分）：已解出赛题、比赛中看过答案的赛题、进入时间 */
const contestSolvedKey = (contestId: string) => `nexora:code:contestSolved:${contestId}`;
const contestCheatKey = (contestId: string) => `nexora:code:contestCheat:${contestId}`;
const contestStartedKey = (contestId: string) => `nexora:code:contestStarted:${contestId}`;

function stageLabelOf(stage: string): string {
  return getStageOption(stage)?.label || '通用';
}

/** 读取 localStorage 中的字符串数组（容错：损坏 / 旧格式返回空数组） */
function readStringList(key: string): string[] {
  try {
    const raw = window.localStorage.getItem(key);
    if (!raw) {
      return [];
    }
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? parsed.filter((item): item is string => typeof item === 'string') : [];
  } catch {
    return [];
  }
}

function readNumber(key: string): number {
  const raw = window.localStorage.getItem(key);
  const value = raw == null ? 0 : Number(raw);
  return Number.isFinite(value) ? value : 0;
}

function writeJson(key: string, value: unknown): void {
  try {
    window.localStorage.setItem(key, JSON.stringify(value));
  } catch {
    // 隐私模式下写入可能失败，不阻断学习
  }
}

function writeNumber(key: string, value: number): void {
  try {
    window.localStorage.setItem(key, String(value));
  } catch {
    // 同上
  }
}

/** 比赛会话（同一页面切换的比赛模式状态） */
interface ContestSession {
  contest: CodingContestVO;
  /** 进入比赛时间戳（本地记录，用于「限时」倒计时；服务端不下发 startedAt） */
  startedAt: number;
  /** 是否已提交成绩 */
  submitted: boolean;
  /** 复盘模式（已提交 / 比赛已结束，可看答案） */
  review: boolean;
}

/** 成绩卡（提交后展示） */
interface ScoreCard {
  score: number;
  solvedCount: number;
  totalCount: number;
  /** 用时（秒） */
  duration: number;
}

/** 看答案的计分口径：practice 30% / contest 不计分 / review 复盘免费 */
type ReferenceMode = 'practice' | 'contest' | 'review';

/**
 * AI 编程实验室（在线题库 + 编程比赛）。
 *
 * 与技术路线对齐：写作环境（Monaco）+ 浏览器内 Python（Pyodide）+ AI 全程陪学（右侧栏复用
 * /agent/sendMessage + Netty WebSocket 流式）+ **在线题库判分**（/codingProblem/judge，服务端比对，
 * 预期输出不下发）+ **编程比赛**（报名 / 倒计时 / 教练模式 / 提交成绩 / 复盘看解析）。
 *
 * 通关、积分与「看过答案」记录在 localStorage（按学段），比赛过程按比赛记录；
 * 参考答案按比赛规则由服务端裁决（比赛进行中默认拒绝，提交或结束后放行）。
 */
export default function Coding() {
  const { message, modal } = App.useApp();
  const [searchParams, setSearchParams] = useSearchParams();
  const userInfo = useAuthStore((state) => state.userInfo);
  const token = useAuthStore((state) => state.token);
  const stage = userInfo?.stage || 'JUNIOR';
  const stageLabel = stageLabelOf(stage);

  // ==================== 题库 / 练习状态 ====================
  const [problems, setProblems] = useState<CodingProblemVO[]>([]);
  const [problemsStage, setProblemsStage] = useState<string>();
  const [problemsLoading, setProblemsLoading] = useState(false);
  const [currentProblemId, setCurrentProblemId] = useState<string>();
  const [passedIds, setPassedIds] = useState<string[]>([]);
  const [answerUsedIds, setAnswerUsedIds] = useState<string[]>([]);
  const [stageScore, setStageScore] = useState(0);
  const [combo, setCombo] = useState(0);
  const [code, setCode] = useState('');
  const [showingAnswer, setShowingAnswer] = useState(false);
  const [answerSnapshot, setAnswerSnapshot] = useState<string | null>(null);
  const [referenceNotes, setReferenceNotes] = useState<string>();

  // ==================== 比赛状态 ====================
  const [contests, setContests] = useState<CodingContestVO[]>([]);
  const [contestsLoading, setContestsLoading] = useState(false);
  const [activeTab, setActiveTab] = useState<CodingTabKey>('practice');
  const [enteringContestId, setEnteringContestId] = useState<string | null>(null);
  const [contestSession, setContestSession] = useState<ContestSession | null>(null);
  const [contestSolvedIds, setContestSolvedIds] = useState<string[]>([]);
  const [contestCheatIds, setContestCheatIds] = useState<string[]>([]);
  const [submitting, setSubmitting] = useState(false);
  const [scoreCard, setScoreCard] = useState<ScoreCard | null>(null);
  const [now, setNow] = useState(() => Date.now());

  // ==================== 运行 / 判分 ====================
  const [running, setRunning] = useState(false);
  const [judging, setJudging] = useState(false);
  const [saving, setSaving] = useState(false);
  const editorRef = useRef<MonacoEditorNs.IStandaloneCodeEditor | null>(null);
  const askAiRef = useRef<(question: string) => void>(() => undefined);
  const { status, error: runtimeError, preload, run, restart } = usePyodide();
  const [result, setResult] = useState<Awaited<ReturnType<typeof run>> | null>(null);

  // ==================== 工具：草稿与代码引用 ====================
  const stageRef = useRef(stage);
  const codeRef = useRef(code);
  const problemIdRef = useRef<string | undefined>(currentProblemId);
  const referenceCacheRef = useRef<{ problemId: string; code: string; notes?: string } | null>(null);

  useEffect(() => {
    codeRef.current = code;
  }, [code]);
  useEffect(() => {
    problemIdRef.current = currentProblemId;
  }, [currentProblemId]);

  /** 切题前把当前编辑内容立即落盘（自动保存有 400ms 防抖，避免切题丢最后几个字符） */
  const flushDraft = useCallback(() => {
    const id = problemIdRef.current;
    const value = codeRef.current;
    if (!id || !value) {
      return;
    }
    try {
      window.localStorage.setItem(draftKey(stage, id), value);
    } catch {
      // 忽略写入失败
    }
  }, [stage]);

  const resetReferenceState = useCallback(() => {
    setShowingAnswer(false);
    setAnswerSnapshot(null);
    setReferenceNotes(undefined);
    referenceCacheRef.current = null;
  }, []);

  const applyProblem = useCallback((problem: CodingProblemVO) => {
    flushDraft();
    setCurrentProblemId(problem.problemId);
    let draft: string | null = null;
    try {
      draft = window.localStorage.getItem(draftKey(stage, problem.problemId));
    } catch {
      // 读取失败按无草稿处理
    }
    setCode(draft ?? problem.starterCode ?? '');
    setResult(null);
    resetReferenceState();
  }, [flushDraft, resetReferenceState, stage]);

  // ==================== 数据加载 ====================
  const loadProblems = useCallback(async (targetStage: string) => {
    setProblemsLoading(true);
    try {
      const list = await loadCodingProblems();
      if (stageRef.current !== targetStage) {
        return;
      }
      // 服务端已按难度排序，这里再按「难度 → 排序」稳定一遍，保证「挑战下一题」口径一致
      const ordered = [...(list ?? [])].sort(
        (a, b) => (a.difficulty ?? 99) - (b.difficulty ?? 99) || (a.sort ?? 0) - (b.sort ?? 0),
      );
      setProblems(ordered);
      setProblemsStage(targetStage);
    } catch {
      if (stageRef.current === targetStage) {
        setProblems([]);
        setProblemsStage(targetStage);
      }
    } finally {
      if (stageRef.current === targetStage) {
        setProblemsLoading(false);
      }
    }
  }, []);

  const loadContests = useCallback(async () => {
    setContestsLoading(true);
    try {
      const list = await loadCodingContests();
      setContests(list ?? []);
    } catch {
      setContests([]);
    } finally {
      setContestsLoading(false);
    }
  }, []);

  // 学段切换：重置全部状态，恢复该学段的历史记录，并重新拉取题库与比赛
  useEffect(() => {
    stageRef.current = stage;
    setPassedIds(readStringList(passedKey(stage)));
    setAnswerUsedIds(readStringList(answerUsedKey(stage)));
    setStageScore(readNumber(scoreKey(stage)));
    setCombo(0);
    setContestSession(null);
    setContestSolvedIds([]);
    setContestCheatIds([]);
    setScoreCard(null);
    setCurrentProblemId(undefined);
    setCode('');
    setResult(null);
    resetReferenceState();
    setProblems([]);
    setProblemsStage(undefined);
    void loadProblems(stage);
    void loadContests();
  }, [loadContests, loadProblems, resetReferenceState, stage]);

  // 进入页面即预热 Python 运行时（首装 10-30 秒，提前加载减少等待）
  useEffect(() => {
    void preload().catch(() => undefined);
  }, [preload]);

  // 草稿自动保存（防刷新丢失）
  useEffect(() => {
    if (!code || !currentProblemId) {
      return undefined;
    }
    const timer = window.setTimeout(() => {
      try {
        window.localStorage.setItem(draftKey(stage, currentProblemId), code);
      } catch {
        // 忽略写入失败
      }
    }, 400);
    return () => window.clearTimeout(timer);
  }, [code, currentProblemId, stage]);

  // 题库就绪后默认选中：未通关里难度最低的一道
  useEffect(() => {
    if (contestSession || currentProblemId || problemsStage !== stage || problems.length === 0) {
      return;
    }
    const passedSet = new Set(passedIds);
    applyProblem(problems.find((problem) => !passedSet.has(problem.problemId)) ?? problems[0]);
  }, [applyProblem, contestSession, currentProblemId, passedIds, problems, problemsStage, stage]);

  // ==================== 倒计时 ====================
  const activeProblems = useMemo(
    () => (contestSession ? contestSession.contest.problems ?? [] : problems),
    [contestSession, problems],
  );
  const currentProblem = useMemo(
    () => activeProblems.find((problem) => problem.problemId === currentProblemId),
    [activeProblems, currentProblemId],
  );

  const runningContest = useMemo(() => {
    if (contestSession) {
      return null;
    }
    return contests.find(
      (contest) => contestPhaseOf(contest) === 'running' && contest.myStatus !== 3,
    ) ?? null;
  }, [contestSession, contests]);

  const deadline = useMemo(
    () => (contestSession ? contestDeadlineOf(contestSession.contest, contestSession.startedAt) : null),
    [contestSession],
  );
  const remainingMs = deadline == null ? null : Math.max(0, deadline - now);
  const contestTimeUp = deadline != null && remainingMs === 0 && !contestSession?.submitted;

  const needsTick = Boolean(runningContest)
    || Boolean(contestSession && !contestSession.submitted && !contestSession.review);
  useEffect(() => {
    setNow(Date.now());
    const timer = window.setInterval(() => setNow(Date.now()), needsTick ? 1000 : 30000);
    return () => window.clearInterval(timer);
  }, [needsTick]);

  // ==================== 比赛数据 ====================
  const contestProblems = useMemo(
    () => contestSession?.contest.problems ?? [],
    [contestSession],
  );
  const contestScore = useMemo(
    () => contestProblems
      .filter((problem) => contestSolvedIds.includes(problem.problemId) && !contestCheatIds.includes(problem.problemId))
      .reduce((sum, problem) => sum + (problem.score ?? 0), 0),
    [contestCheatIds, contestProblems, contestSolvedIds],
  );
  const contestSolvedCount = useMemo(
    () => contestProblems.filter((problem) => contestSolvedIds.includes(problem.problemId)).length,
    [contestProblems, contestSolvedIds],
  );

  const contestSessionView: ContestSessionView | null = useMemo(
    () => (contestSession ? {
      contest: contestSession.contest,
      submitted: contestSession.submitted,
      review: contestSession.review,
      solvedIds: contestSolvedIds,
      score: contestScore,
    } : null),
    [contestScore, contestSession, contestSolvedIds],
  );

  /** 进入比赛/复盘：统一装配会话状态并选中第一道未解题 */
  const applyContestVo = useCallback((
    vo: CodingContestVO,
    options?: { submitted?: boolean; review?: boolean },
  ) => {
    const submitted = options?.submitted ?? vo.myStatus === 3;
    const review = options?.review ?? submitted;
    const storedStartedAt = readNumber(contestStartedKey(vo.contestId));
    const startedAt = storedStartedAt > 0 ? storedStartedAt : Date.now();
    writeNumber(contestStartedKey(vo.contestId), startedAt);
    const solved = readStringList(contestSolvedKey(vo.contestId));
    const cheated = readStringList(contestCheatKey(vo.contestId));
    setContestSolvedIds(solved);
    setContestCheatIds(cheated);
    setContestSession({ contest: vo, startedAt, submitted, review });
    setScoreCard(submitted ? {
      score: vo.myScore ?? 0,
      solvedCount: vo.mySolvedCount ?? solved.length,
      totalCount: vo.problemCount ?? (vo.problems?.length ?? 0),
      duration: vo.myDuration ?? 0,
    } : null);
    setActiveTab('contest');
    setSearchParams((prev) => {
      const next = new URLSearchParams(prev);
      next.set('tab', 'contest');
      next.delete('contest');
      return next;
    }, { replace: true });

    const list = vo.problems ?? [];
    if (list.length > 0) {
      const solvedSet = new Set(solved);
      applyProblem(list.find((problem) => !solvedSet.has(problem.problemId)) ?? list[0]);
    } else {
      setCurrentProblemId(undefined);
      setCode('');
    }
  }, [applyProblem, setSearchParams]);

  /** 进入比赛（补报名） */
  const handleEnterContest = useCallback(async (contest: CodingContestVO) => {
    setEnteringContestId(contest.contestId);
    try {
      const vo = await startCodingContest(contest.contestId);
      applyContestVo(vo, { submitted: vo.myStatus === 3, review: vo.myStatus === 3 });
      message.success(vo.myStatus === 3 ? '已进入复盘模式' : '已进入比赛，加油！');
    } catch {
      // 请求层已统一提示（未开始 / 已结束 / 非本学段）
    } finally {
      setEnteringContestId(null);
    }
  }, [applyContestVo, message]);

  /** 从 URL（?contest=xxx）自动进入：进行中直接进，已结束转复盘 */
  const enterContestById = useCallback(async (contestId: string) => {
    setEnteringContestId(contestId);
    try {
      const vo = await startCodingContest(contestId);
      applyContestVo(vo, { submitted: vo.myStatus === 3, review: vo.myStatus === 3 });
    } catch {
      try {
        const vo = await loadCodingContestInfo(contestId);
        if (contestPhaseOf(vo) === 'upcoming' && vo.myStatus !== 3) {
          message.info('比赛还没开始，先做好准备吧');
          return;
        }
        applyContestVo(vo, { submitted: vo.myStatus === 3, review: true });
      } catch {
        // 请求层已统一提示
      }
    } finally {
      setEnteringContestId(null);
    }
  }, [applyContestVo, message]);

  /** 查看复盘 / 查看赛题与解析（已提交或已结束，答案由服务端放行） */
  const handleReviewContest = useCallback(async (contest: CodingContestVO) => {
    setEnteringContestId(contest.contestId);
    try {
      const vo = await loadCodingContestInfo(contest.contestId);
      applyContestVo(vo, { submitted: vo.myStatus === 3, review: true });
    } catch {
      // 请求层已统一提示
    } finally {
      setEnteringContestId(null);
    }
  }, [applyContestVo]);

  /** 报名参加 */
  const handleEnroll = useCallback(async (contest: CodingContestVO) => {
    setEnteringContestId(contest.contestId);
    try {
      await enrollCodingContest(contest.contestId);
      message.success(`已报名「${contest.title}」，开赛后进入比赛即可作答`);
      await loadContests();
    } catch {
      // 请求层已统一提示
    } finally {
      setEnteringContestId(null);
    }
  }, [loadContests, message]);

  /** 退出比赛：未提交时提醒进度会保留（已在 localStorage） */
  const handleExitContest = useCallback(() => {
    const session = contestSession;
    if (!session) {
      return;
    }
    const doExit = () => {
      setContestSession(null);
      setScoreCard(null);
      setContestSolvedIds([]);
      setContestCheatIds([]);
      setCurrentProblemId(undefined);
      setCode('');
      setResult(null);
      resetReferenceState();
      setActiveTab('practice');
      setSearchParams((prev) => {
        const next = new URLSearchParams(prev);
        next.delete('contest');
        next.set('tab', 'practice');
        return next;
      }, { replace: true });
      void loadContests();
    };
    if (!session.submitted && !session.review) {
      modal.confirm({
        title: '退出比赛',
        content: '比赛尚未提交，退出后可从比赛列表再次进入继续作答（已解出的题目会保留）。确定退出吗？',
        okText: '退出比赛',
        cancelText: '继续比赛',
        onOk: doExit,
      });
      return;
    }
    doExit();
  }, [contestSession, loadContests, modal, resetReferenceState, setSearchParams]);

  /** 提交比赛成绩（客户端判分，服务端记录） */
  const handleSubmitContest = useCallback(() => {
    if (!contestSession || submitting) {
      return;
    }
    const session = contestSession;
    const deadlineAt = contestDeadlineOf(session.contest, session.startedAt) ?? Date.now();
    const durationSec = Math.max(0, Math.round((Math.min(Date.now(), deadlineAt) - session.startedAt) / 1000));
    modal.confirm({
      title: '提交比赛成绩',
      content: `提交后不能修改。本次得分 ${contestScore}，解出 ${contestSolvedCount}/${contestProblems.length} 题，用时 ${formatDuration(durationSec)}。确定提交吗？`,
      okText: '确认提交',
      cancelText: '再检查一下',
      onOk: async () => {
        setSubmitting(true);
        try {
          await submitCodingContest(session.contest.contestId, contestScore, contestSolvedCount, durationSec);
          setContestSession((prev) => (prev ? { ...prev, submitted: true, review: true } : prev));
          setScoreCard({
            score: contestScore,
            solvedCount: contestSolvedCount,
            totalCount: contestProblems.length,
            duration: durationSec,
          });
          message.success('比赛成绩已提交，可以看解析复盘了');
          void loadContests();
        } finally {
          setSubmitting(false);
        }
      },
    });
  }, [contestProblems.length, contestScore, contestSession, contestSolvedCount, loadContests, message, modal, submitting]);

  // URL 参数：?tab=contest 打开比赛页签，?contest=xxx 自动进入比赛
  const queryContestId = searchParams.get('contest');
  const queryTabKey = searchParams.get('tab');
  const queryHandledRef = useRef(false);
  useEffect(() => {
    if (queryTabKey === 'contest') {
      setActiveTab('contest');
    }
  }, [queryTabKey]);
  useEffect(() => {
    if (!queryContestId || queryHandledRef.current) {
      return;
    }
    queryHandledRef.current = true;
    setActiveTab('contest');
    void enterContestById(queryContestId);
  }, [enterContestById, queryContestId]);

  const handleTabChange = useCallback((key: CodingTabKey) => {
    setActiveTab(key);
    setSearchParams((prev) => {
      const next = new URLSearchParams(prev);
      next.set('tab', key);
      if (key === 'practice') {
        next.delete('contest');
      }
      return next;
    }, { replace: true });
  }, [setSearchParams]);

  // ==================== 判分与积分 ====================
  const handlePassed = useCallback((problem: CodingProblemVO) => {
    if (contestSession) {
      if (contestSolvedIds.includes(problem.problemId)) {
        message.success('又一次通过，继续保持！');
        return;
      }
      const nextSolved = [...contestSolvedIds, problem.problemId];
      setContestSolvedIds(nextSolved);
      writeJson(contestSolvedKey(contestSession.contest.contestId), nextSolved);
      if (contestCheatIds.includes(problem.problemId)) {
        message.success(`解出「${problem.title}」，但比赛中看过答案，本题不计分`);
      } else {
        setCombo((value) => value + 1);
        message.success(`解出「${problem.title}」，比赛得分 +${problem.score ?? 0}`);
      }
      return;
    }
    if (passedIds.includes(problem.problemId)) {
      message.success('又一次通过，继续保持！');
      return;
    }
    const discounted = answerUsedIds.includes(problem.problemId);
    const gained = Math.round((problem.score ?? 0) * (discounted ? 0.3 : 1));
    const nextPassed = [...passedIds, problem.problemId];
    const nextScore = stageScore + gained;
    setPassedIds(nextPassed);
    writeJson(passedKey(stage), nextPassed);
    setStageScore(nextScore);
    writeNumber(scoreKey(stage), nextScore);
    if (!discounted) {
      setCombo((value) => value + 1);
    }
    message.success(`通关「${problem.title}」⭐ +${gained} 积分${discounted ? '（看过答案，按 30% 计分）' : ''}`);
  }, [answerUsedIds, contestCheatIds, contestSession, contestSolvedIds, message, passedIds, stage, stageScore]);

  const handleRun = useCallback(async () => {
    if (!code.trim()) {
      message.warning('先写点代码再运行吧');
      return;
    }
    if (running || judging || !currentProblem) {
      return;
    }
    setRunning(true);
    setResult(null);
    try {
      const next = await run(code);
      setResult(next);
      if (next.error) {
        setCombo(0);
        return;
      }
      setJudging(true);
      try {
        // 判分只上报运行输出：优先 stdout（print 结果），没有输出时退化为最后一个表达式值
        const output = next.stdout.length > 0 ? next.stdout.join('\n') : (next.value ?? '');
        const verdict = await judgeCodingProblem(currentProblem.problemId, output);
        if (verdict.passed) {
          handlePassed(currentProblem);
        } else {
          setCombo(0);
          message.warning(verdict.message
            || '还没有通过：请检查输出的内容、顺序与空格是否与题目要求一致，再试一次。');
        }
      } finally {
        setJudging(false);
      }
    } catch (error: any) {
      message.error(error?.message || 'Python 运行环境加载失败，请点右上角「重启环境」重试');
    } finally {
      setRunning(false);
    }
  }, [code, currentProblem, handlePassed, judging, message, run, running]);

  // ==================== 显示答案 / 恢复我的代码 ====================
  const fetchReference = useCallback(async (mode: ReferenceMode) => {
    if (!currentProblem) {
      return;
    }
    const problem = currentProblem;
    try {
      const ref = await loadCodingReference(problem.problemId, contestSession?.contest.contestId);
      if (ref?.denyReason) {
        message.warning(ref.denyReason);
        return;
      }
      if (!ref?.referenceCode) {
        message.info('这道题暂时没有参考答案，再想想吧');
        return;
      }
      referenceCacheRef.current = {
        problemId: problem.problemId,
        code: ref.referenceCode,
        notes: ref.solutionNotes,
      };
      setAnswerSnapshot(code);
      setCode(ref.referenceCode);
      editorRef.current?.setValue(ref.referenceCode);
      setReferenceNotes(ref.solutionNotes);
      setShowingAnswer(true);
      if (mode === 'practice') {
        if (!answerUsedIds.includes(problem.problemId)) {
          const next = [...answerUsedIds, problem.problemId];
          setAnswerUsedIds(next);
          writeJson(answerUsedKey(stage), next);
        }
        setCombo(0);
        message.info('已记录：本题之后通过只计 30% 积分');
      } else if (mode === 'contest') {
        const contestId = contestSession?.contest.contestId;
        if (contestId && !contestCheatIds.includes(problem.problemId)) {
          const next = [...contestCheatIds, problem.problemId];
          setContestCheatIds(next);
          writeJson(contestCheatKey(contestId), next);
        }
        setCombo(0);
        message.warning('比赛中看过答案，本题不计分');
      }
    } catch {
      // 请求层已统一提示
    }
  }, [answerUsedIds, code, contestCheatIds, contestSession, currentProblem, message, stage]);

  /** 显示答案（练习二次确认 30% 计分；比赛中需 allowAnswer=1；复盘直接看） */
  const handleToggleReference = useCallback(() => {
    if (!currentProblem) {
      return;
    }
    const cache = referenceCacheRef.current;
    if (showingAnswer) {
      // 恢复我的代码（缓存保留，可再次切到答案）
      if (answerSnapshot !== null) {
        setCode(answerSnapshot);
        editorRef.current?.setValue(answerSnapshot);
      }
      setShowingAnswer(false);
      return;
    }
    if (cache && cache.problemId === currentProblem.problemId) {
      setAnswerSnapshot(code);
      setCode(cache.code);
      editorRef.current?.setValue(cache.code);
      setReferenceNotes(cache.notes);
      setShowingAnswer(true);
      return;
    }
    const review = Boolean(contestSession?.review || contestSession?.submitted);
    if (review) {
      void fetchReference('review');
      return;
    }
    if (contestSession) {
      modal.confirm({
        title: '查看答案',
        content: '比赛期间查看答案后本题不计分（解出仍可计入题数），确定要看吗？',
        okText: '确定要看',
        cancelText: '再想想',
        onOk: () => void fetchReference('contest'),
      });
      return;
    }
    modal.confirm({
      title: '查看答案',
      content: '看答案后本题只计 30% 积分，仍可点亮 ⭐，确定要看吗？',
      okText: '确定要看',
      cancelText: '再想想',
      onOk: () => void fetchReference('practice'),
    });
  }, [answerSnapshot, code, contestSession, currentProblem, fetchReference, modal, showingAnswer]);

  /** 复盘模式下「看解析」入口：直接展示当前题的参考答案与讲解 */
  const handleOpenSolution = useCallback(() => {
    if (!currentProblem) {
      return;
    }
    const cache = referenceCacheRef.current;
    if (cache && cache.problemId === currentProblem.problemId) {
      if (!showingAnswer) {
        setAnswerSnapshot(code);
        setCode(cache.code);
        editorRef.current?.setValue(cache.code);
        setReferenceNotes(cache.notes);
        setShowingAnswer(true);
      }
      return;
    }
    void fetchReference('review');
  }, [code, currentProblem, fetchReference, showingAnswer]);

  // ==================== 编辑器工具 ====================
  const handleResetCode = useCallback(() => {
    const starter = currentProblem?.starterCode ?? '';
    setCode(starter);
    editorRef.current?.setValue(starter);
    setResult(null);
    resetReferenceState();
  }, [currentProblem, resetReferenceState]);

  const handleNextChallenge = useCallback(() => {
    const passedSet = new Set(passedIds);
    const next = problems.find((problem) => !passedSet.has(problem.problemId));
    if (!next) {
      message.success('本学段题目已全部通关，太棒了！');
      return;
    }
    applyProblem(next);
    message.info(`下一题：${next.title}`);
  }, [applyProblem, message, passedIds, problems]);

  /** 当前题之后的下一题（通关提示条用） */
  const nextProblemAfterCurrent = useMemo(() => {
    if (!currentProblem) {
      return undefined;
    }
    const index = problems.findIndex((problem) => problem.problemId === currentProblem.problemId);
    return index >= 0 ? problems[index + 1] : undefined;
  }, [currentProblem, problems]);

  const handleRestartRuntime = useCallback(async () => {
    await restart();
    setResult(null);
    message.info('Python 环境已重置，下次运行会重新加载（首次约 10-30 秒）');
  }, [message, restart]);

  const handleExport = useCallback(() => {
    const blob = new Blob([code], { type: 'text/x-python;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `${currentProblem?.title || '编程练习'}.py`;
    link.click();
    URL.revokeObjectURL(url);
  }, [code, currentProblem]);

  const handleSave = useCallback(async () => {
    if (!token) {
      message.warning('登录后才能保存到个人知识库');
      return;
    }
    if (!code.trim()) {
      message.warning('没有可保存的代码');
      return;
    }
    setSaving(true);
    try {
      const stamp = new Date().toLocaleString('zh-CN', { hour12: false }).replace(/[/: ]/g, '-');
      const name = `编程练习-${currentProblem?.title || stageLabel}-${stamp}`;
      const outputText = result
        ? [...result.stdout, result.error ?? ''].filter(Boolean).join('\n')
        : '';
      const markdown = [
        `# ${name}`,
        '',
        `- 学段：${stageLabel}`,
        currentProblem ? `- 题目：${currentProblem.title}（${currentProblem.goal}）` : '',
        '',
        '## 我的代码',
        '',
        '```python',
        code,
        '```',
        outputText ? `\n## 运行输出\n\n\`\`\`\n${outputText}\n\`\`\`\n` : '',
      ].filter((line) => line !== '').join('\n');
      const file = new File([markdown], `${name}.md`, { type: 'text/markdown' });
      const session = await prepareStudentUpload({
        resourceName: name,
        resourceType: 'DOCUMENT',
        fileName: `${name}.md`,
        fileSize: file.size,
      });
      await uploadStudentShard(session.uploadId, 0, file.slice(0, file.size));
      message.success('已保存到「原始资料」，可在知识中心生成知识页');
    } catch {
      // 错误已由请求层提示
    } finally {
      setSaving(false);
    }
  }, [code, currentProblem, message, result, stageLabel, token]);

  // 快捷键：Ctrl / ⌘ + Enter 运行，Ctrl / ⌘ + S 保存
  useEffect(() => {
    const handler = (event: KeyboardEvent) => {
      const withMeta = event.ctrlKey || event.metaKey;
      if (!withMeta) {
        return;
      }
      if (event.key === 'Enter') {
        event.preventDefault();
        void handleRun();
      } else if (event.key.toLowerCase() === 's') {
        event.preventDefault();
        void handleSave();
      }
    };
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, [handleRun, handleSave]);

  // ==================== 展示派生 ====================
  const contestMode = Boolean(contestSession);
  const coachMode = Boolean(contestSession && !contestSession.submitted && !contestSession.review);
  const answerDisabled = Boolean(
    contestSession && !contestSession.review && !contestSession.submitted
      && contestSession.contest.allowAnswer !== 1,
  );
  const bannerRemaining = runningContest
    ? Math.max(0, (parseTime(runningContest.endTime) ?? now) - now)
    : null;
  const bannerSolved = useMemo(() => {
    if (!runningContest) {
      return 0;
    }
    const local = readStringList(contestSolvedKey(runningContest.contestId)).length;
    return Math.max(local, runningContest.mySolvedCount ?? 0);
  }, [runningContest]);

  const runtimeText = running
    ? '运行中…'
    : judging
      ? '判分中…'
      : status === 'loading'
        ? 'Python 环境加载中（首次约 10-30 秒）…'
        : status === 'error'
          ? '环境加载失败'
          : result
            ? `耗时 ${result.durationMs} ms`
            : '首次运行会自动加载本地 Python 环境';

  return (
    <div className={styles.codingPage}>
      <header className={styles.pageHeader}>
        <div className={styles.pageTitleBlock}>
          <span className={styles.logo}>
            <Terminal size={20} />
          </span>
          <div>
            <h1 className={styles.pageTitle}>AI 编程实验室</h1>
            <p className={styles.pageDesc}>
              在线题库 + 编程比赛：在浏览器里写 Python，运行时问 AI，边写边学
            </p>
          </div>
        </div>

        <div className={styles.headerRight}>
          <Tag className={styles.stageTag} bordered={false}>{stageLabel}</Tag>
          <Tooltip title="已通关题目数">
            <span className={styles.starBadge}>
              <Star size={14} fill="#FFA970" color="#FFA970" />
              {passedIds.length}
            </span>
          </Tooltip>
          <Space size={8}>
            <Tooltip title="保存到「原始资料」（可在知识中心生成知识页）">
              <Button icon={<Save size={15} />} loading={saving} onClick={() => void handleSave()}>
                保存学习
              </Button>
            </Tooltip>
            <Tooltip title="导出为 .py 文件">
              <Button icon={<Download size={15} />} onClick={handleExport}>
                导出
              </Button>
            </Tooltip>
            <Tooltip title="清空 Python 变量与导入，重建运行环境">
              <Button icon={<RefreshCw size={15} />} onClick={() => void handleRestartRuntime()}>
                重启环境
              </Button>
            </Tooltip>
          </Space>
        </div>
      </header>

      {contestSession ? (
        <div className={styles.contestBar}>
          <span className={styles.contestBarTitle}>
            <Trophy size={15} />
            {contestSession.contest.title}
          </span>
          <Tag color={contestSession.submitted ? 'default' : 'orange'} bordered={false}>
            {contestSession.submitted ? '已提交' : contestSession.review ? '复盘模式' : '比赛进行中'}
          </Tag>
          {!contestSession.submitted && !contestSession.review ? (
            <span className={contestTimeUp ? styles.countdownDanger : styles.countdown}>
              <Timer size={14} />
              {contestTimeUp ? '已到截止时间，请尽快提交' : `剩余 ${formatCountdown(remainingMs ?? 0)}`}
            </span>
          ) : null}
          {scoreCard ? (
            <span className={styles.scoreCard}>
              得分 {scoreCard.score} · 解出 {scoreCard.solvedCount}/{scoreCard.totalCount} · 用时 {formatDuration(scoreCard.duration)}
            </span>
          ) : (
            <span className={styles.scoreCard}>
              已解出 {contestSolvedCount}/{contestProblems.length} · 当前得分 {contestScore}
            </span>
          )}
          <span className={styles.contestBarActions}>
            {contestSession.submitted || contestSession.review ? (
              <Button size="small" icon={<Eye size={14} />} onClick={handleOpenSolution}>
                看解析
              </Button>
            ) : null}
            {!contestSession.submitted ? (
              <Button
                size="small"
                type="primary"
                icon={<Flag size={14} />}
                loading={submitting}
                onClick={handleSubmitContest}
              >
                提交比赛
              </Button>
            ) : null}
            <Button size="small" icon={<LogOut size={14} />} onClick={handleExitContest}>
              退出比赛
            </Button>
          </span>
        </div>
      ) : runningContest ? (
        <div className={styles.contestBanner}>
          <span className={styles.contestBannerTitle}>
            <Trophy size={16} />
            {runningContest.title}
          </span>
          <span className={styles.contestBannerCountdown}>
            <Timer size={14} />
            距结束 {formatCountdown(bannerRemaining ?? 0)}
          </span>
          <span className={styles.contestBannerProgress}>
            已完成 {bannerSolved}/{runningContest.problemCount ?? 0}
          </span>
          <Button
            size="small"
            type="primary"
            icon={<Flag size={14} />}
            loading={enteringContestId === runningContest.contestId}
            onClick={() => void handleEnterContest(runningContest)}
          >
            进入比赛
          </Button>
        </div>
      ) : null}

      {status === 'error' ? (
        <div className={styles.runtimeAlert}>
          Python 运行环境加载失败：{runtimeError}。可点右上角「重启环境」重试，或检查网络后刷新页面。
        </div>
      ) : null}

      <div className={styles.codingBody}>
        <ProblemPanel
          stageLabel={stageLabel}
          problems={problems}
          problemsLoading={problemsLoading}
          passedIds={passedIds}
          currentProblemId={currentProblemId}
          stageScore={stageScore}
          combo={combo}
          onSelectProblem={applyProblem}
          onNextChallenge={handleNextChallenge}
          activeTab={activeTab}
          onTabChange={handleTabChange}
          contests={contests}
          contestsLoading={contestsLoading}
          enteringContestId={enteringContestId}
          onEnroll={(contest) => void handleEnroll(contest)}
          onEnterContest={(contest) => void handleEnterContest(contest)}
          onReviewContest={(contest) => void handleReviewContest(contest)}
          contestSession={contestSessionView}
        />

        <section className={styles.workbench}>
          <div className={styles.workbenchBar}>
            <Space size={8}>
              <Button
                type="primary"
                icon={<Play size={15} />}
                loading={running || judging}
                onClick={() => void handleRun()}
              >
                运行
              </Button>
              <Button icon={<RotateCcw size={15} />} onClick={handleResetCode}>
                重置代码
              </Button>
              <Tooltip
                title={answerDisabled
                  ? '比赛中已禁用显示答案，提交或比赛结束后可复盘查看'
                  : showingAnswer
                    ? '回到我写的代码（可再次切回答案）'
                    : '查看参考答案与解析'}
              >
                <Button
                  icon={showingAnswer ? <EyeOff size={15} /> : <Eye size={15} />}
                  disabled={answerDisabled || !currentProblem}
                  onClick={handleToggleReference}
                >
                  {showingAnswer ? '恢复我的代码' : '显示答案'}
                </Button>
              </Tooltip>
            </Space>
            <span className={styles.runtimeInfo}>{runtimeText}</span>
          </div>

          {!contestMode && currentProblem && passedIds.includes(currentProblem.problemId) ? (
            <div className={styles.passBar}>
              <span className={styles.passText}>
                <Star size={13} fill="#FFA970" color="#FFA970" />
                已通关，积分已记录
              </span>
              {nextProblemAfterCurrent ? (
                <Button
                  type="link"
                  size="small"
                  onClick={() => applyProblem(nextProblemAfterCurrent)}
                >
                  下一题：{nextProblemAfterCurrent.title} →
                </Button>
              ) : (
                <span className={styles.passText}>已经是最后一题啦</span>
              )}
            </div>
          ) : null}

          <div className={styles.editorBox}>
            <Editor
              height="100%"
              language={PYTHON_TYPE}
              value={code}
              onChange={(value) => setCode(value || '')}
              beforeMount={(monaco) => defineCodingThemes(monaco)}
              theme={MONACO_LIGHT}
              onMount={(instance) => {
                editorRef.current = instance;
              }}
              loading={<div className={styles.editorLoading}>编辑器加载中…</div>}
              options={{
                fontSize: 14,
                minimap: { enabled: false },
                scrollBeyondLastLine: false,
                automaticLayout: true,
                tabSize: 4,
                smoothScrolling: true,
                padding: { top: 10, bottom: 10 },
                renderLineHighlight: 'all',
              }}
            />
          </div>

          {referenceNotes ? (
            <div className={styles.solutionBox}>
              <div className={styles.solutionHead}>
                <Eye size={13} />
                <span>参考解析</span>
                <Button
                  type="text"
                  size="small"
                  className={styles.solutionClose}
                  icon={<EyeOff size={13} />}
                  onClick={() => setReferenceNotes(undefined)}
                />
              </div>
              <p className={styles.solutionBody}>{referenceNotes}</p>
            </div>
          ) : null}

          <RunConsole
            running={running || judging}
            result={result}
            onAskAi={(question) => askAiRef.current(question)}
            onClear={() => setResult(null)}
            onCopy={() => {
              const text = result
                ? [...result.stdout, ...result.stderr, result.error ?? ''].filter(Boolean).join('\n')
                : '';
              void navigator.clipboard?.writeText(text);
              message.success('已复制运行输出');
            }}
          />
        </section>

        <AiCoachPanel
          code={code}
          taskTitle={currentProblem?.title}
          stageLabel={stageLabel}
          lastRun={result
            ? { stdout: result.stdout, stderr: result.stderr, error: result.error }
            : null}
          coachMode={coachMode}
          onApplyCode={(snippet) => {
            setCode(snippet);
            editorRef.current?.setValue(snippet);
          }}
          registerAsk={(fn) => {
            askAiRef.current = fn;
          }}
        />
      </div>
    </div>
  );
}
