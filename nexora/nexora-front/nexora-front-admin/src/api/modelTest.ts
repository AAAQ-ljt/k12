import { request } from './request';

/** 向量模型测试结果 */
export interface EmbeddingTestVO {
  dimension: number;
  sample: string;
}

/** 文生图测试结果 */
export interface ImageTestVO {
  success: boolean;
  url?: string;
  message: string;
}

/** 对话模型连通性 */
export function modelTestChat(text: string): Promise<string> {
  return request.post('/modelTest/chat', { text }, { timeout: 120000 });
}

/** 向量模型连通性 */
export function modelTestEmbedding(text: string): Promise<EmbeddingTestVO> {
  return request.post('/modelTest/embedding', { text }, { timeout: 120000 });
}

/** 文生图模型连通性 */
export function modelTestImage(prompt: string): Promise<ImageTestVO> {
  // qwen-image-2.0-pro 同步出图实测约 112s，前端等待上限放到 5 分钟，避免先于后端报超时
  return request.post('/modelTest/image', { prompt }, { timeout: 300000 });
}
