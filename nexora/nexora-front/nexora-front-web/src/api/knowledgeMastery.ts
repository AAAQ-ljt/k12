import { get } from './request';

/** 知识点掌握度明细项 */
export interface MasteryItem {
  knowledgePointId: string;
  knowledgePointName: string;
  stage?: string;
  /** 掌握度 0-100（历史正确率） */
  masteryScore: number;
  /** 状态：0未解锁 1进行中 2已掌握 */
  status: number;
  practiceCount: number;
  correctCount: number;
  lastPracticeTime?: string | null;
  nextReviewTime?: string | null;
  /** 是否已到复习时间 */
  due?: boolean;
}

/** 我的学习进度（掌握度概览，数据来自判分回写） */
export interface MasteryOverview {
  masteredCount: number;
  learningCount: number;
  totalCount: number;
  avgMasteryScore: number;
  totalPractice: number;
  totalCorrect: number;
  correctRate: number;
  dueReviewCount: number;
  items: MasteryItem[];
}

export function loadMyMasteryOverview(limit = 20): Promise<MasteryOverview> {
  return get('/knowledgeMastery/myOverview', { limit });
}
