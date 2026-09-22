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

/** 学习路径 AI 生成任务（异步编排：Redis 状态机 + 前端轮询） */
export interface LearningPathGenTask {
  taskId: string;
  /** PENDING / PATH_GENERATING / COMPLETED / FAILED */
  status: string;
  message?: string;
  title?: string;
  pathId?: string;
  createTime?: string;
  updateTime?: string;
}

/** 生成学习路径（需先填写学习目标）——提交异步任务，立即返回 taskId，前端轮询 /genTask 获取进度 */
export function generateLearningPath(): Promise<LearningPathGenTask> {
  return post('/learningPath/generate');
}

/** 查询学习路径生成任务进度（生成期间按钮禁用，防止重复生成） */
export function getLearningPathGenTask(taskId: string): Promise<LearningPathGenTask> {
  return get('/learningPath/genTask', { taskId });
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

/** 节点快测题（含答案，答题阶段不展示 answer/analysis；svg 为图表题题干配图） */
export interface NodeQuizQuestion {
  index: number;
  type: string;
  question: string;
  options: string[];
  answer: number;
  analysis?: string;
  /** 题干 SVG 配图（图表类知识点，渲染前需清洗） */
  svg?: string;
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

/** 节点快测异步出题任务（Redis 状态机：PENDING → QUIZ_GENERATING → COMPLETED/FAILED，前端轮询） */
export interface NodeQuizTask {
  taskId: string;
  itemId: string;
  knowledgePointId?: string;
  knowledgePointName?: string;
  status: string;
  message?: string;
  /** 完成后的题目 JSON（与 NodeQuiz 同构，含答案与解析；答题阶段不展示 answer/analysis） */
  quizJson?: string;
  createTime?: string;
  updateTime?: string;
}

/** 提交节点快测出题任务：立即返回 taskId，前端轮询 /nodeQuizTask 获取进度；生成期间按钮禁用防重复出题 */
export function genNodeQuiz(itemId: string): Promise<NodeQuizTask> {
  return post('/learningPath/genNodeQuiz', { itemId });
}

/** 查询节点快测出题任务进度 */
export function getNodeQuizTask(taskId: string): Promise<NodeQuizTask> {
  return get('/learningPath/nodeQuizTask', { taskId });
}

/** 将快测任务题目 JSON 解析为答题卡（补全 task 层的节点信息与题号） */
export function parseNodeQuizTask(task: NodeQuizTask): NodeQuiz | null {
  if (!task.quizJson) {
    return {
      itemId: task.itemId,
      knowledgePointId: task.knowledgePointId ?? '',
      knowledgePointName: task.knowledgePointName ?? '',
      title: '节点快测',
      questions: [],
    };
  }
  try {
    const parsed = JSON.parse(task.quizJson);
    const questions = (parsed.questions ?? []).map((q: NodeQuizQuestion, i: number) => ({
      index: i,
      type: q.type ?? 'SINGLE',
      question: q.question ?? '',
      options: Array.isArray(q.options) ? q.options : [],
      answer: typeof q.answer === 'number' ? q.answer : 0,
      analysis: q.analysis ?? '',
      svg: typeof q.svg === 'string' ? q.svg : '',
    }));
    return {
      itemId: task.itemId,
      knowledgePointId: task.knowledgePointId ?? '',
      knowledgePointName: task.knowledgePointName ?? '',
      title: parsed.title ?? '节点快测',
      questions,
    };
  } catch {
    return null;
  }
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
