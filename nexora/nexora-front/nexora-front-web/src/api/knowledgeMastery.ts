import { get, post } from './request';

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

  /** 该知识点所属的学习路径节点 ID（「就地复习快测」用；不在任何路线里则为空） */
  itemId?: string | null;
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

/** 复习提醒静音（复习闭环）：until=today 今天不用提醒 / forever 不再提醒该知识点 */
export function muteReviewReminder(knowledgePointId: string, until: 'today' | 'forever' = 'today') {
  return post('/knowledgeMastery/muteReview', null, { params: { knowledgePointId, until } });
}

/** 恢复复习提醒 */
export function unmuteReviewReminder(knowledgePointId: string) {
  return post('/knowledgeMastery/unmuteReview', null, { params: { knowledgePointId } });
}
