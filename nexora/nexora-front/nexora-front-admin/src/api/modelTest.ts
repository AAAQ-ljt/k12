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
  return request.post('/modelTest/image', { prompt }, { timeout: 120000 });
}