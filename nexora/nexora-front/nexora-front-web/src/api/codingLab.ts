import { get, post } from './request';

/**
 * AI 编程实验室（在线题库 + 编程比赛）接口。
 *
 * 与后端学生端控制器一一对应：
 * - StudentCodingProblemController：/codingProblem/list、/getInfo、/judge、/reference
 * - StudentCodingContestController：/codingContest/list、/myList、/getInfo、/enroll、/start、/submit、/rank
 *
 * 判分在服务端完成（预期输出/关键词不下发前端），前端只上报运行输出；
 * 参考答案单独走 /reference（比赛进行中默认拒绝）。
 */

/** 编程题（学生端视图，不含参考答案） */
export interface CodingProblemVO {
  problemId: string;
  /** 学段：PRIMARY_HIGH / JUNIOR / SENIOR */
  stage: string;
  /** 难度 1-4（入门 / 基础 / 进阶 / 挑战） */
  difficulty: number;
  title: string;
  /** 一句话目标 */
  goal: string;
  description?: string;
  hint?: string;
  starterCode?: string;
  /** 本题积分（10/20/30/50） */
  score: number;
  estimateMinutes?: number;
  sort?: number;
  language?: string;
  knowledgePointId?: string;
  /** 是否已通关（二期由后端提供，本期恒为 null，前端以 localStorage 为准） */
  passed?: boolean | null;
}

/** 编程题参考答案（仅在允许查看时下发） */
export interface CodingProblemReferenceVO {
  problemId: string;
  referenceCode?: string;
  solutionNotes?: string;
  /** 本次查看是否影响积分：练习模式看答案后只计 30% */
  scoreDiscounted?: boolean;
  /** 不允许查看的原因（如「比赛进行中不可查看答案」） */
  denyReason?: string;
}

/** 比赛视图（我的状态：0未报名 1已报名 2比赛中 3已提交） */
export interface CodingContestVO {
  contestId: string;
  title: string;
  stage: string;
  description?: string;
  startTime?: string;
  endTime?: string;
  durationMinutes?: number;
  /** 1 表示比赛进行中允许查看答案 */
  allowAnswer?: number;
  /** 发布状态（1 已发布） */
  status?: number;
  problemCount?: number;
  totalScore?: number;
  myStatus: number;
  myScore?: number | null;
  mySolvedCount?: number | null;
  myDuration?: number | null;
  submitCount?: number;
  /** 赛题列表（仅详情/进入比赛接口下发） */
  problems?: CodingProblemVO[];
}

/** 比赛报名/成绩记录（排行榜） */
export interface CodingContestRecordVO {
  recordId: number;
  contestId: string;
  contestTitle?: string;
  stage?: string;
  /** 本人为 "me"，其他人为脱敏标识（同学xxxx） */
  userId: string;
  /** 0已报名 1比赛中 2已提交 */
  status: number;
  enrollTime?: string;
  submittedAt?: string;
  score: number;
  solvedCount: number;
  totalCount: number;
  /** 用时（秒） */
  duration: number;
  /** 名次 */
  rank: number;
}

/** 题库列表（按难度由易到难，服务端排序）；难度/关键词可选过滤 */
export function loadCodingProblems(params?: {
  difficulty?: number;
  keyword?: string;
}): Promise<CodingProblemVO[]> {
  return get('/codingProblem/list', params ?? {});
}

/** 题目详情（含预置代码与提示，不含答案） */
export function loadCodingProblemInfo(problemId: string): Promise<CodingProblemVO> {
  return get('/codingProblem/getInfo', { problemId });
}

/** 判分结果（当前后端返回 boolean，预留服务端提示文案） */
export interface CodingJudgeResult {
  passed: boolean;
  /** 服务端提示文案；后端仅返回 boolean 时为空，前端回退默认文案 */
  message?: string;
}

/**
 * 判分：只上报运行输出，由服务端比对，返回是否通过。
 * 兼容两种响应形态：boolean（当前实现）与 { passed, message }（后端后续下沉提示文案时直接生效）。
 */
export function judgeCodingProblem(problemId: string, output: string): Promise<CodingJudgeResult> {
  return post<boolean | { passed?: boolean; message?: string }>(
    '/codingProblem/judge',
    { problemId, output },
  ).then((data) => {
    if (typeof data === 'boolean') {
      return { passed: data };
    }
    return { passed: Boolean(data?.passed), message: data?.message };
  });
}

/**
 * 参考答案（「显示答案」）。
 * contestId 非空表示比赛模式下查看：比赛进行中默认拒绝，提交或比赛结束后放行。
 */
export function loadCodingReference(
  problemId: string,
  contestId?: string,
): Promise<CodingProblemReferenceVO> {
  return get('/codingProblem/reference', contestId ? { problemId, contestId } : { problemId });
}

/** 本学段、已发布未结束的比赛 */
export function loadCodingContests(): Promise<CodingContestVO[]> {
  return get('/codingContest/list');
}

/** 我报名/参加过的比赛（含已结束，「我的」页用） */
export function loadMyCodingContests(): Promise<CodingContestVO[]> {
  return get('/codingContest/myList');
}

/** 比赛详情（含赛题列表，不含答案） */
export function loadCodingContestInfo(contestId: string): Promise<CodingContestVO> {
  return get('/codingContest/getInfo', { contestId });
}

/** 报名参加比赛 */
export function enrollCodingContest(contestId: string): Promise<void> {
  return post('/codingContest/enroll', null, { params: { contestId } });
}

/** 进入比赛（自动补报名，返回含赛题列表的比赛详情） */
export function startCodingContest(contestId: string): Promise<CodingContestVO> {
  return post('/codingContest/start', null, { params: { contestId } });
}

/** 提交比赛成绩（一期为客户端判定成绩：得分 / 解出题数 / 用时秒数） */
export function submitCodingContest(
  contestId: string,
  score: number,
  solvedCount: number,
  duration: number,
): Promise<void> {
  return post('/codingContest/submit', null, { params: { contestId, score, solvedCount, duration } });
}

/** 比赛排行榜（我的行 userId 为 "me"，其他人已脱敏为同学xxxx） */
export function loadCodingContestRank(
  contestId: string,
  limit = 50,
): Promise<CodingContestRecordVO[]> {
  return get('/codingContest/rank', { contestId, limit });
}
