import { post } from './request';

/** AI 对话发送参数 */
export interface AgentMessageParams {
  sessionId?: string;
  message: string;
  mode?: 'chat' | 'animation' | 'picture_book';
}

/** AI 对话发送结果 */
export interface AgentMessageResult {
  messageId: string;
  sessionId: string;
}

// TODO: 后端 AgentController.sendMessage 就绪后接入
export function sendAgentMessage(_params: AgentMessageParams): Promise<AgentMessageResult> {
  return post('/agent/sendMessage', _params);
}

// TODO: 后端取消接口就绪后接入
export function cancelAgentMessage(_messageId: string): Promise<void> {
  return post('/agent/cancelMessage', { messageId: _messageId });
}
