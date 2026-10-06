import { request } from './request';
import type { PageParam, PageResult } from '@/types/common';

/** 比赛保存实体（对应后端 CodingContest PO） */
export interface CodingContest {
  contestId?: string;
  title: string;
  /** 面向学段：PRIMARY_HIGH / JUNIOR / SENIOR */
  stage: string;
  /** 比赛说明（Markdown） */
  description?: string;
  /** 开始时间 yyyy-MM-dd HH:mm:ss */
  startTime: string;
  /** 结束时间 yyyy-MM-dd HH:mm:ss */
  endTime: string;
  /** 单场限时（分钟，空=不限时） */
  durationMinutes?: number | null;
  /** 比赛期间是否允许看答案：0否 1是 */
  allowAnswer?: number;
  /** 0草稿 1已发布 2已结束 */
  status?: number;
}

/** 比赛详情中的赛题项（对应后端 CodingProblemVO 裁剪字段） */
export interface CodingContestProblemItem {
  problemId: string;
  stage?: string;
  difficulty?: number;
  title?: string;
  goal?: string;
  /** 该题在本场比赛中的分值 */
  score: number;
  sort?: number;
}

/** 比赛视图（对应后端 CodingContestVO） */
export interface CodingContestVO {
  contestId: string;
  title: string;
  stage: string;
  description?: string;
  startTime?: string;
  endTime?: string;
  durationMinutes?: number;
  allowAnswer?: number;
  status: number;
  /** 赛题数 */
  problemCount?: number;
  /** 总分 */
  totalScore?: number;
  /** 已提交人数 */
  submitCount?: number;
  /** 赛题列表（仅详情接口下发） */
  problems?: CodingContestProblemItem[];
}

/** 比赛查询参数 */
export interface CodingContestQuery extends PageParam {
  titleFuzzy?: string;
  stage?: string;
  status?: number;
}

/** 参赛记录（对应后端 CodingContestRecord） */
export interface CodingContestRecord {
  recordId: number;
  contestId: string;
  userId: string;
  stage?: string;
  /** 0已报名 1比赛中 2已提交 */
  status: number;
  enrollTime?: string;
  startedAt?: string;
  submittedAt?: string;
  score?: number;
  solvedCount?: number;
  totalCount?: number;
  /** 用时（秒） */
  duration?: number;
}

/** 赛题编排入参：按数组顺序保存，score 为逐题分值 */
export interface ContestProblemSaveItem {
  problemId: string;
  score: number;
}

/** 分页加载比赛列表 */
export function loadDataList(query: CodingContestQuery): Promise<PageResult<CodingContestVO>> {
  return request.get('/codingContest/loadDataList', { params: query });
}

/** 获取比赛详情（含赛题编排） */
export function getInfo(contestId: string): Promise<CodingContestVO> {
  return request.get('/codingContest/getInfo', { params: { contestId } });
}

/** 新增比赛 */
export function addContest(data: CodingContest): Promise<string> {
  return request.post('/codingContest/add', data);
}

/** 修改比赛 */
export function updateContest(data: CodingContest): Promise<void> {
  return request.put('/codingContest/update', data);
}

/** 发布比赛（草稿 → 已发布） */
export function publishContest(contestId: string): Promise<void> {
  return request.put('/codingContest/publish', null, { params: { contestId } });
}

/** 提前结束比赛 */
export function finishContest(contestId: string): Promise<void> {
  return request.put('/codingContest/finish', null, { params: { contestId } });
}

/** 删除比赛 */
export function delContest(contestId: string): Promise<void> {
  return request.delete('/codingContest/del', { params: { contestId } });
}

/** 赛题编排：按数组顺序保存，逐题可覆盖分值 */
export function saveProblems(
  contestId: string,
  problems: ContestProblemSaveItem[],
): Promise<void> {
  return request.post('/codingContest/saveProblems', problems, { params: { contestId } });
}

/** 参赛记录（谁报名 / 得分） */
export function recordList(contestId: string): Promise<CodingContestRecord[]> {
  return request.get('/codingContest/recordList', { params: { contestId } });
}
