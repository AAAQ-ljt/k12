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
