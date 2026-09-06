import { request } from './request';
import type { PageParam, PageResult } from '@/types/common';

/** 答题批阅列表条目 */
export interface PracticeReviewItem {
  recordId: number;
  userId: string;
  username?: string;
  nickName?: string;
  grade?: string;
  questionId: string;
  questionTitle: string;
  questionType: number;
  questionMaxScore?: number;
  correctAnswer?: string;
  analysis?: string;
  knowledgePointName?: string;
  subject?: string;
  userAnswer?: string;
  score?: number;
  source?: number;
  bizId?: string;
  lessonName?: string;
  reviewStatus: number;
  reviewScore?: number;
  reviewerId?: string;
  reviewComment?: string;
  reviewTime?: string;
  createTime?: string;
}

/** 批阅统计 */
export interface PracticeReviewStats {
  pendingCount: number;
  reviewedCount: number;
  reviewAvgScore: number;
  objectiveAccuracy: number;
}

/** 批阅列表查询参数 */
export interface PracticeReviewQueryParam extends PageParam {
  grade?: string;
  subject?: string;
  questionType?: number;
  source?: number;
  /** 0待批阅 1已批阅 */
  reviewStatus?: number;
  studentFuzzy?: string;
}

/** 批阅列表 */
export function loadReviewList(query: PracticeReviewQueryParam): Promise<PageResult<PracticeReviewItem>> {
  return request.get('/questionReview/loadDataList', { params: query });
}

/** 批阅统计 */
export function getReviewStats(): Promise<PracticeReviewStats> {
  return request.get('/questionReview/stats');
}

/** 提交批阅 */
export function submitReview(data: { recordId: number; reviewScore: number; reviewComment?: string }): Promise<void> {
  return request.post('/questionReview/review', data);
}
