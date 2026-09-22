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

/** 近 7 天单日练习 */
export interface TrendDay {
  day: string;
  count: number;
}

/** 我的学习趋势（学习天数/连续打卡/练习次数） */
export interface LearningTrend {
  studyDays: number;
  streakDays: number;
  totalPractice: number;
  weekTrend: TrendDay[];
}

export function loadMyLearningTrend(): Promise<LearningTrend> {
  return get('/knowledgeMastery/loadMyTrend');
}

/** 待复习知识点定位结果 */
export interface ReviewLocate {
  /** 是否在我的学习路径中找到对应节点 */
  located: boolean;
  pathId?: string;
  itemId?: string;
  knowledgePointName?: string;
}

export function locateReviewPoint(knowledgePointId: string): Promise<ReviewLocate> {
  return get('/knowledgeMastery/locateReview', { knowledgePointId });
}
