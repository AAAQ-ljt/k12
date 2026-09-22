import { del, get, post } from './request';

/** 路径节点（三态由掌握度驱动：0未解锁 1进行中 2已掌握） */
export interface LearningPathNode {
  itemId: string;
  knowledgePointId: string;
  knowledgePointName: string;
  /** 0主线 1兴趣分支 */
  branchType: number;
  branchName?: string | null;
  /** 0学习 1复习 */
  itemType: number;
  status: number;
  sort: number;
  dueDate?: string | null;
  finishTime?: string | null;
  masteryScore: number;
  practiceCount: number;
  nextReviewTime?: string | null;
  /** 是否已到复习时间 */
  due?: boolean;
  /** 学习建议（知识点描述） */
  learningTip?: string | null;
  /** 要做什么（叙事层：可验收的动手任务） */
  task?: string | null;
  /** 怎么学（叙事层） */
  way?: string | null;
  /** 预计用时（分钟） */
  minutes?: number | null;
  /** 是否必学（false=选学） */
  must?: boolean | null;
  /** 未解锁时的前置节点名 */
  prerequisiteName?: string | null;
}

/** 阶段（含阶段目标与验收口径） */
export interface LearningPathStage {
  name: string;
  goal?: string | null;
  checkpoint?: string | null;
  nodes: LearningPathNode[];
  finished?: boolean;
}

export interface LearningPathBranch {
  branchName: string;
  nodes: LearningPathNode[];
}

/** 路线库卡片（列表用，不含节点明细） */
export interface LearningPathSummary {
  pathId: string;
  title: string;
  goal?: string | null;
  stage?: string;
  /** 0进行中 1已完成 2已放弃 */
  status: number;
  totalItems: number;
  finishedItems: number;
  progress: number;
  currentNodeName?: string | null;
  stageCount?: number | null;
  /** 旧版生成（无目标/阶段） */
  legacy?: boolean;
  createTime?: string;
  updateTime?: string;
}

/** 路线详情（阶段化节点 + 叙事层） */
export interface LearningPathDetail {
  pathId: string;
  title: string;
  stage?: string;
  status: number;
  totalItems: number;
  finishedItems: number;
  progress: number;
  currentNodeId?: string | null;
  currentNodeName?: string | null;
  /** 总目标 */
  goal?: string | null;
  /** 产出物 */
  outcome?: string | null;
  /** 建议节奏 */
  cadence?: string | null;
  /** 起点建议 */
  startHint?: string | null;
  createTime?: string;
  updateTime?: string;
  stages: LearningPathStage[];
  branches: LearningPathBranch[];
}

/** 历史计划（旧版无叙事层的生成记录） */
export interface LearningPathHistoryItem {
  recordId: string;
  title?: string;
  content?: string;
  stage?: string;
  createTime?: string;
}

/** 旧版计划（steps / mainLine 结构） */
export interface LegacyPlan {
  title: string;
  steps: { title: string; desc?: string; task?: string; kind?: string }[];
}

export function parseLegacyPlan(content?: string): LegacyPlan | null {
  if (!content) {
    return null;
  }
  try {
    const parsed = JSON.parse(content);
    if (!parsed || typeof parsed !== 'object') {
      return null;
    }
    const steps = Array.isArray(parsed.steps)
      ? parsed.steps
      : Array.isArray(parsed.mainLine)
        ? parsed.mainLine
        : null;
    if (!steps) {
      return null;
    }
    return { title: parsed.title ?? '', steps };
  } catch {
    return null;
  }
}

/** 生成学习路径（需先填写学习目标） */
export function generateLearningPath(): Promise<LearningPathDetail> {
  return post('/learningPath/generate');
}

/** 我的路线库（卡片列表） */
export function loadMyLearningPaths(): Promise<LearningPathSummary[]> {
  return get('/learningPath/myList');
}

/** 路线详情 */
export function loadLearningPathDetail(pathId: string): Promise<LearningPathDetail> {
  return get('/learningPath/detail', { pathId });
}

/** 删除路线 */
export function deleteLearningPath(pathId: string): Promise<void> {
  return del('/learningPath/del', { pathId });
}

/** 历史计划 */
export function loadLearningPathHistory(): Promise<LearningPathHistoryItem[]> {
  return get('/learningPath/historyList');
}

/** 删除历史计划 */
export function deleteLearningPathHistory(recordId: string): Promise<void> {
  return del('/learningPath/historyDel', { recordId });
}

/** 节点快测题（含答案，答题阶段不展示 answer/analysis） */
export interface NodeQuizQuestion {
  index: number;
  type: string;
  question: string;
  options: string[];
  answer: number;
  analysis?: string;
}

/** 节点快测出题响应 */
export interface NodeQuiz {
  itemId: string;
  knowledgePointId: string;
  knowledgePointName: string;
  title: string;
  questions: NodeQuizQuestion[];
}

/** 节点快测逐题判分明细 */
export interface NodeQuizQuestionResult {
  index: number;
  question: string;
  options: string[];
  userAnswer: string;
  correctAnswer: string;
  correct: boolean;
  analysis: string;
}

/** 节点快测提交判分结果 */
export interface NodeQuizResult {
  passed: boolean;
  correctCount: number;
  totalCount: number;
  /** 本次得分率（百分制） */
  score: number;
  /** 回写后掌握度 0-100 */
  masteryScore: number;
  /** 是否已跨入「已掌握」 */
  mastered: boolean;
  results: NodeQuizQuestionResult[];
}

/** 节点快测出题 */
export function genNodeQuiz(itemId: string): Promise<NodeQuiz> {
  return get('/learningPath/genNodeQuiz', { itemId });
}

/** 节点快测提交判分 */
export function submitNodeQuiz(payload: {
  itemId: string;
  duration?: number;
  questions: NodeQuizQuestion[];
  answers: { index: number; userAnswer: string }[];
}): Promise<NodeQuizResult> {
  return post('/learningPath/submitNodeQuiz', payload);
}
