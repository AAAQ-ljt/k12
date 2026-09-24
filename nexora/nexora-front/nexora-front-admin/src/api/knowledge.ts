import { request } from './request';
import type { PageParam, PageResult } from '@/types/common';

/** 知识点 */
export interface KnowledgePoint {
  knowledgePointId: string;
  name: string;
  stage: string;
  subject: string;
  difficulty: number;
  description?: string;
  cover?: string;
  lessonId?: string;
  sort?: number;
  status: number;
  createTime?: string;
  updateTime?: string;
}

/** 知识文档 */
export interface KnowledgeDoc {
  docId: string;
  title: string;
  stage: string;
  knowledgePointId: string;
  difficulty: number;
  dataType: string;
  content?: string;
  sourceType?: number;
  sourceResourceId?: string;
  sourceUrl?: string;
  vectorStatus: number;
  vectorError?: string;
  chunkCount?: number;
  status: number;
  createBy?: number;
  createTime?: string;
  updateTime?: string;
}

export interface KnowledgeDocQuery extends PageParam {
  docId?: string;
  titleFuzzy?: string;
  stage?: string;
  knowledgePointId?: string;
  difficulty?: number;
  vectorStatus?: number;
  status?: number;
}

export interface KnowledgeTreeNode {
  key: string;
  label: string;
  type: 'stage' | 'subject' | 'point';
  stage?: string;
  subject?: string;
  knowledgePointId?: string;
  difficulty?: number;
  docCount?: number;
  children?: KnowledgeTreeNode[];
}

export interface KnowledgeOverview {
  totalDocs: number;
  totalPoints: number;
  totalChunks: number;
  readyDocs: number;
  failedDocs: number;
  expiredDocs: number;
  stageDistribution: Record<string, number>;
  vectorStatusDistribution: Record<string, number>;
}

export interface KnowledgeSearchResult {
  docId: string;
  title: string;
  stage: string;
  knowledgePointId: string;
  difficulty: number;
  chunkIndex: number;
  content: string;
  score: number;
  searchMode: string;
  sourceUrl?: string;
}

export interface KnowledgeSearchTestParams {
  question: string;
  stage?: string;
  knowledgePointId?: string;
  difficulty?: number;
  topK?: number;
  threshold?: number;
}

export interface KnowledgeImportResult {
  successCount: number;
  failedCount: number;
  errors: string[];
}

/** 从资源导入知识文档参数 */
export interface ResourceKnowledgeImportParams {
  resourceId: string;
  title?: string;
  stage: string;
  knowledgePointId: string;
  difficulty: number;
  sourceType?: number; // 1=资料解析 2=手动填写资源说明
  content?: string;
}

/** 从资源导入知识文档结果 */
export interface ResourceKnowledgeImportResult {
  docId: string;
  title: string;
  stage: string;
  knowledgePointId: string;
  difficulty: number;
  sourceType: number;
  sourceResourceId: string;
  contentLength: number;
  chunkCount: number;
  vectorStatus: number;
  warnings: string[];
  async?: boolean;
  /** 解析入库任务 ID（任务状态机轮询用） */
  taskId?: string;
  /** 任务状态：PENDING / EXTRACTING / VECTORIZING / COMPLETED / FAILED */
  taskStatus?: string;
  /** 任务进度 0-100 */
  progress?: number;
  /** 任务阶段说明 / 失败信息 */
  message?: string;
}

/** 知识库解析入库异步任务 */
export interface KnowledgeImportTask {
  taskId: string;
  docId: string;
  resourceId?: string;
  sourceType?: number;
  /** PENDING / EXTRACTING / VECTORIZING / COMPLETED / FAILED */
  status: string;
  message?: string;
  /** 进度 0-100 */
  progress?: number;
  createTime?: string;
  updateTime?: string;
}

/** AI 文档整理结果 */
export interface KnowledgeAIDocVO {
  resourceId: string;
  resourceName?: string;
  stage?: string;
  originalText?: string;
  organizedMd?: string;
}

export function loadOverview(): Promise<KnowledgeOverview> {
  return request.get('/knowledgeBase/overview');
}

export function loadTree(): Promise<KnowledgeTreeNode[]> {
  return request.get('/knowledgeBase/tree');
}

export function loadDocList(query: KnowledgeDocQuery): Promise<PageResult<KnowledgeDoc>> {
  return request.get('/knowledgeBase/docList', { params: query });
}

export function addDoc(data: Partial<KnowledgeDoc>): Promise<void> {
  return request.post('/knowledgeBase/docAdd', data);
}

export function updateDoc(data: Partial<KnowledgeDoc>): Promise<void> {
  return request.put('/knowledgeBase/docUpdate', data);
}

export function delDoc(docId: string): Promise<void> {
  return request.delete('/knowledgeBase/docDel', { params: { docId } });
}

export function addPoint(data: Partial<KnowledgePoint>): Promise<void> {
  return request.post('/knowledgeBase/pointAdd', data);
}

export function updatePoint(data: Partial<KnowledgePoint>): Promise<void> {
  return request.put('/knowledgeBase/pointUpdate', data);
}

export function delPoint(knowledgePointId: string): Promise<void> {
  return request.delete('/knowledgeBase/pointDel', { params: { knowledgePointId } });
}

export function importDir(): Promise<KnowledgeImportResult> {
  return request.post('/knowledgeBase/importDir');
}

export function resourceImport(data: ResourceKnowledgeImportParams): Promise<ResourceKnowledgeImportResult> {
  return request.post('/knowledgeBase/resourceImport', data);
}

/** 查询知识库解析入库任务状态（前端轮询） */
export function getImportTask(taskId: string): Promise<KnowledgeImportTask> {
  return request.get('/knowledgeBase/importTask', { params: { taskId } });
}

export function vectorize(docId: string): Promise<void> {
  return request.post('/knowledgeBase/vectorize', null, { params: { docId } });
}

export function searchTest(params: KnowledgeSearchTestParams): Promise<KnowledgeSearchResult[]> {
  return request.post('/knowledgeBase/searchTest', params);
}

export function aiOrganize(resourceId: string): Promise<KnowledgeAIDocVO> {
  // AI 整理为同步调用,大文档可能耗时较长
  return request.post('/knowledgeBase/aiOrganize', null, { params: { resourceId }, timeout: 300000 });
}

/** AI 文档整理异步任务（Redis 状态机：PENDING → ORGANIZING → COMPLETED/FAILED，前端轮询） */
export interface AiOrganizeTask {
  taskId: string;
  resourceId: string;
  resourceName?: string;
  stage?: string;
  /** PENDING / ORGANIZING / COMPLETED / FAILED */
  status: string;
  message?: string;
  /** 完成后的整理稿 Markdown */
  organizedMd?: string;
  /** 完成后的原始提取文本（对照参考） */
  originalText?: string;
  createTime?: string;
  updateTime?: string;
}

/** 提交 AI 文档整理任务：立即返回 taskId，前端轮询 /aiOrganizeTask 获取进度；切页按 taskId 恢复不丢状态 */
export function submitAiOrganize(resourceId: string): Promise<AiOrganizeTask> {
  return request.post('/knowledgeBase/aiOrganizeTask', null, { params: { resourceId } });
}

/** 查询 AI 文档整理任务状态（前端轮询 / 切页恢复） */
export function getAiOrganizeTask(taskId: string): Promise<AiOrganizeTask> {
  return request.get('/knowledgeBase/aiOrganizeTask', { params: { taskId } });
}
