import { request } from './request';

/** 向量模型测试结果 */
export interface EmbeddingTestVO {
  dimension: number;
  sample: string;
}

/** 文生图测试异步任务体（Redis 持久化，前端轮询；切页可凭 taskId 恢复） */
export interface ImageGenTaskVO {
  taskId: string;
  prompt?: string;
  /** 提交时生效的文生图供应商：dashscope / ark / gpt-image-2 */
  provider?: string;
  /** PENDING / GENERATING / COMPLETED / FAILED */
  status: 'PENDING' | 'GENERATING' | 'COMPLETED' | 'FAILED';
  message?: string;
  imageUrl?: string;
  createTime?: string;
  updateTime?: string;
}

/** 对话模型连通性 */
export function modelTestChat(text: string): Promise<string> {
  return request.post('/modelTest/chat', { text }, { timeout: 120000 });
}

/** 向量模型连通性 */
export function modelTestEmbedding(text: string): Promise<EmbeddingTestVO> {
  return request.post('/modelTest/embedding', { text }, { timeout: 120000 });
}

/** 提交文生图测试任务（异步，立即返回任务体；进行中重复提交返回原任务） */
export function submitImageTest(prompt: string): Promise<ImageGenTaskVO> {
  return request.post('/modelTest/image', { prompt });
}

/** 轮询文生图测试任务状态 */
export function loadImageTestTask(taskId: string): Promise<ImageGenTaskVO> {
  return request.get('/modelTest/imageTask', { params: { taskId } });
}

/** MiMo 预置音色（mimo-v2.5-tts），与后端 TtsProvider 白名单一致 */
export const TTS_VOICE_OPTIONS = [
  'mimo_default',
  '冰糖',
  '茉莉',
  '苏打',
  '白桦',
  'Mia',
  'Chloe',
  'Milo',
  'Dean',
].map((voice) => ({ label: voice, value: voice }));

/** 语音合成测试结果（base64 音频由前端转 Blob 播放） */
export interface TtsTestVO {
  audioBase64: string;
  /** 音频容器格式（mp3） */
  format: string;
  model: string;
  voice: string;
  costMs: number;
  audioBytes: number;
}

/** 语音合成连通性（同步，1~10 秒） */
export function modelTestTts(params: { text: string; voice?: string; tone?: string }): Promise<TtsTestVO> {
  return request.post('/modelTest/tts', params, { timeout: 120000 });
}