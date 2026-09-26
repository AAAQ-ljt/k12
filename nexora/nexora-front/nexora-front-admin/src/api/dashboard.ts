import { request } from './request';

/** 近 7 天对话趋势项 */
export interface DashboardTrendItem {
  dayLabel: string;
  count: number;
}

/** 学段用户分布项 */
export interface DashboardStageItem {
  stage: string;
  count: number;
}

/** 最近 AI 会话项 */
export interface DashboardSessionItem {
  sessionId: string;
  title: string;
  messageCount?: number;
  lastMessageTime?: string;
}

/** 待办事项项 */
export interface DashboardTodoItem {
  key: string;
  label: string;
  count: number;
}

/** AI 消耗趋势项（近 7 天，按天汇总） */
export interface DashboardUsageTrendItem {
  dayLabel: string;
  promptTokens: number;
  completionTokens: number;
  imageCount: number;
}

/** 控制面板总览 */
export interface DashboardOverview {
  userCount: number;
  todayActiveCount: number;
  knowledgeDocCount: number;
  aiMessageCount: number;
  totalTokenCount: number;
  imageGenCount: number;
  /** 语音合成次数（TTS 旁白按次累计） */
  ttsGenCount: number;
  trend: DashboardTrendItem[];
  usageTrend: DashboardUsageTrendItem[];
  stageDist: DashboardStageItem[];
  recentSessions: DashboardSessionItem[];
  todos: DashboardTodoItem[];
}

/** 控制面板总览数据 */
export function loadDashboardOverview(): Promise<DashboardOverview> {
  return request.get('/dashboard/overview');
}