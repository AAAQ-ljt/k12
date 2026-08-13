import { get, post } from './request';

/** AI 会话信息 */
export interface AgentSessionInfo {
  sessionId: string;
  userId: string;
  title: string;
  stage: string;
  scene?: number;
  messageCount?: number;
  lastMessageTime?: string;
  status?: number;
  createTime?: string;
  updateTime?: string;
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
}

/** AI 对话发送结果 */
export interface AgentSendResult {
  messageId: string;
  sessionId: string;
}

/** WebSocket 流式推送消息 */
export interface AgentPushMessage {
  messageId: string;
  sessionId?: string;
  type: 'outputting' | 'done' | 'error';
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

/** 加载会话列表 */
export function loadAgentSessionList(): Promise<AgentSessionInfo[]> {
  return get('/agent/sessionList');
}

/** 加载指定会话的历史消息 */
export function loadAgentHistory(sessionId: string): Promise<AgentMessageInfo[]> {
  return get('/agent/loadHistoryMessage', { sessionId });
}

/** 删除会话 */
export function deleteAgentSession(sessionId: string): Promise<void> {
  return post('/agent/delSession', { sessionId });
}
