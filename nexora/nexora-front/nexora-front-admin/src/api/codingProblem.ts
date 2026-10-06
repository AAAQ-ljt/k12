import { request } from './request';
import type { PageParam, PageResult } from '@/types/common';

/** 编程题（对应后端 CodingProblem PO） */
export interface CodingProblem {
  problemId?: string;
  /** 学段：PRIMARY_HIGH / JUNIOR / SENIOR */
  stage: string;
  /** 年级（可空） */
  grade?: string;
  /** 知识点（可空，二期用于掌握度回写） */
  knowledgePointId?: string;
  /** 难度：1入门 2基础 3进阶 4挑战 */
  difficulty: number;
  title: string;
  /** 一句话目标 */
  goal?: string;
  /** 题目描述（Markdown） */
  description?: string;
  /** 思路提示 */
  hint?: string;
  /** 预置代码框架 */
  starterCode?: string;
  /** 参考答案（必填，学生点「显示答案」时下发） */
  referenceCode: string;
  /** 答案讲解 */
  solutionNotes?: string;
  /** 判定方式：1关键词包含 2输出精确匹配 3正则 */
  judgeType: number;
  /** 期望输出（judgeType=2） */
  expectedOutput?: string;
  /** 期望关键词，逗号分隔（judgeType=1） */
  expectedKeywords?: string;
  /** 期望正则（judgeType=3） */
  expectedPattern?: string;
  /** 积分 */
  score: number;
  /** 预估时长（分钟） */
  estimateMinutes?: number;
  /** 同难度内排序 */
  sort?: number;
  /** 运行语言，默认 python */
  language?: string;
  /** 0下架 1上架 */
  status?: number;
  createTime?: string;
  updateTime?: string;
}

/** 编程题查询参数 */
export interface CodingProblemQuery extends PageParam {
  titleFuzzy?: string;
  stage?: string;
  difficulty?: number;
  status?: number;
}

/** 分页加载编程题列表 */
export function loadDataList(query: CodingProblemQuery): Promise<PageResult<CodingProblem>> {
  return request.get('/codingProblem/loadDataList', { params: query });
}

/** 获取编程题详情（含参考答案） */
export function getInfo(problemId: string): Promise<CodingProblem> {
  return request.get('/codingProblem/getInfo', { params: { problemId } });
}

/** 新增编程题 */
export function addProblem(data: CodingProblem): Promise<string> {
  return request.post('/codingProblem/add', data);
}

/** 修改编程题 */
export function updateProblem(data: CodingProblem): Promise<void> {
  return request.put('/codingProblem/update', data);
}

/** 上架 / 下架：status 1上架 0下架 */
export function changeStatus(problemId: string, status: number): Promise<void> {
  return request.put('/codingProblem/changeStatus', null, {
    params: { problemId, status },
  });
}

/** 删除编程题 */
export function delProblem(problemId: string): Promise<void> {
  return request.delete('/codingProblem/del', { params: { problemId } });
}
