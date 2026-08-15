import { request } from './request';
import type { PageParam, PageResult } from '@/types/common';

/** 考试实体 */
export interface ExamInfo {
  examId: string;
  examName: string;
  stage?: string;
  grade?: string;
  paperId: string;
  paperName?: string;
  startTime?: string;
  endTime?: string;
  durationMinutes?: number;
  status?: number;
  createBy?: number;
  createTime?: string;
  updateTime?: string;
}

/** 考试查询参数 */
export interface ExamInfoQuery extends PageParam {
  examNameFuzzy?: string;
  grade?: string;
  paperId?: string;
  status?: number;
}

/** 分页加载考试 */
export function loadDataList(query: ExamInfoQuery): Promise<PageResult<ExamInfo>> {
  return request.get('/examInfo/loadDataList', { params: query });
}

/** 获取考试详情 */
export function getInfo(examId: string): Promise<ExamInfo> {
  return request.get('/examInfo/getInfo', { params: { examId } });
}

/** 保存考试 */
export function saveExam(data: Partial<ExamInfo>): Promise<void> {
  return request.post('/examInfo/save', data);
}

/** 删除考试 */
export function delExam(examId: string): Promise<void> {
  return request.delete('/examInfo/del', { params: { examId } });
}
