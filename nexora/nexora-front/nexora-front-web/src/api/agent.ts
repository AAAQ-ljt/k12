import { get, post } from './request';

/** AI 会话信息 */
export interface AgentSessionInfo {
  sessionId: string;
  userId: string;
  title: string;
  stage: string;
  /** 会话场景：0 自由对话 / 3 编程练习 */
  scene?: number;
  /** 置顶：0 否 / 1 是（列表已按 top desc, last_message_time desc 排序） */
  top?: number;
  messageCount?: number;
  lastMessageTime?: string;
  status?: number;
  createTime?: string;
  updateTime?: string;
}

/** 会话列表筛选参数：scene 只看某场景，sceneNot 排除某场景（3 = 编程练习） */
export interface AgentSessionListParams {
  scene?: number;
  sceneNot?: number;
}

/** AI 消息信息 */
export interface AgentMessageInfo {
  messageId: string;
  sessionId: string;
  userId: string;
  stage?: string;
  userMessage: string;
  assistantMessage?: string;
  intent?: string;
  bizType?: string;
  bizData?: string;
  status?: number;
  errorInfo?: string;
  createTime?: string;
  updateTime?: string;
}

/** AI 对话发送参数 */
export interface AgentSendParams {
  sessionId?: string;
  message: string;
  /** 随消息携带的图片资源ID（个人库 IMAGE），可空 */
  imageResourceIds?: string[];
  /** 新会话场景（仅新建会话时生效）：0 自由对话 / 3 编程练习 */
  scene?: number;
  /** 新会话标题（仅新建会话时生效，如「编程练习 · 星星塔」） */
  sessionTitle?: string;
}

/** AI 对话发送结果 */
export interface AgentSendResult {
  messageId: string;
  sessionId: string;
}

/** AI 助教资料推荐卡片 */
export interface ResourceRecommendItem {
  docId: string;
  title: string;
  resourceId?: string;
  resourceType?: string;
  sourceUrl?: string;
  /** 资源归属：空 = 公共/课程资源；非空 = 该学生的个人资源（跳「知识中心」预览） */
  ownerId?: string;
}

/** WebSocket 流式推送消息 */
export interface AgentPushMessage {
  messageId: string;
  sessionId?: string;
  type: 'outputting' | 'done' | 'error' | 'recommend';
  content?: string;
  bizType?: string;
  bizData?: string;
}

/** 发送 AI 消息 */
export function sendAgentMessage(params: AgentSendParams): Promise<AgentSendResult> {
  return post('/agent/sendMessage', params);
}

/** 取消正在生成的 AI 回复 */
export function cancelAgentMessage(messageId: string): Promise<void> {
  return post('/agent/cancelMessage', { messageId });
}

/** 新建 AI 会话 */
export function createAgentSession(): Promise<AgentSessionInfo> {
  return post('/agent/createSession');
}

/** 加载会话列表（不传参 = 全部；sceneNot: 3 = 只看普通对话；scene: 3 = 只看编程练习） */
export function loadAgentSessionList(params?: AgentSessionListParams): Promise<AgentSessionInfo[]> {
  return get('/agent/sessionList', params);
}

/** 重命名会话 */
export function renameAgentSession(sessionId: string, title: string): Promise<void> {
  return post('/agent/renameSession', { sessionId, title });
}

/** 置顶 / 取消置顶会话（top: 1 置顶 / 0 取消） */
export function topAgentSession(sessionId: string, top: number): Promise<void> {
  return post('/agent/topSession', { sessionId, top });
}

/** 加载指定会话的历史消息 */
export function loadAgentHistory(sessionId: string): Promise<AgentMessageInfo[]> {
  return get('/agent/loadHistoryMessage', { sessionId });
}

/** 删除会话 */
export function deleteAgentSession(sessionId: string): Promise<void> {
  return post('/agent/delSession', { sessionId });
}
