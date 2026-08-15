import { request } from './request';
import type { PageParam, PageResult } from '@/types/common';

/** 题目选项 */
export interface QuestionOption {
  optionId?: number;
  questionId?: string;
  optionLabel?: string;
  optionContent: string;
  isAnswer: number;
  sort?: number;
  createTime?: string;
}

/** 题目实体（对应后端 QuestionInfo PO） */
export interface QuestionInfo {
  questionId: string;
  knowledgePointId?: string;
  stage?: string;
  grade?: string;
  difficulty: number;
  questionType: number;
  title: string;
  questionImage?: string;
  answer?: string;
  analysis?: string;
  source?: number;
  auditStatus?: number;
  score?: number;
  status?: number;
  createBy?: number;
  createTime?: string;
  updateTime?: string;
}

/** 题目保存参数 */
export interface QuestionSaveDTO {
  question: Partial<QuestionInfo>;
  options?: QuestionOption[];
}

/** 题目详情 */
export interface QuestionDetail {
  question: QuestionInfo;
  options: QuestionOption[];
}

/** 题目查询参数 */
export interface QuestionInfoQuery extends PageParam {
  titleFuzzy?: string;
  grade?: string;
  questionType?: number;
  difficulty?: number;
  auditStatus?: number;
  status?: number;
  source?: number;
}

/** 分页加载题目列表 */
export function loadDataList(query: QuestionInfoQuery): Promise<PageResult<QuestionInfo>> {
  return request.get('/questionInfo/loadDataList', { params: query });
}

/** 获取题目详情 */
export function getInfo(questionId: string): Promise<QuestionDetail> {
  return request.get('/questionInfo/getInfo', { params: { questionId } });
}

/** 新增题目 */
export function addQuestion(data: QuestionSaveDTO): Promise<string> {
  return request.post('/questionInfo/add', data);
}

/** 修改题目 */
export function updateQuestion(data: QuestionSaveDTO): Promise<void> {
  return request.put('/questionInfo/update', data);
}

/** 删除题目 */
export function delQuestion(questionId: string): Promise<void> {
  return request.delete('/questionInfo/del', { params: { questionId } });
}

/** 审核题目：0 待审核 1 已上架 2 已驳回 */
export function auditQuestion(questionId: string, auditStatus: number): Promise<void> {
  return request.put('/questionInfo/audit', null, {
    params: { questionId, auditStatus },
  });
}
