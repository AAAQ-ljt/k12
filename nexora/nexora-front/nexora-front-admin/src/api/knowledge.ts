import { request } from './request';
import type { PageParam, PageResult } from '@/types/common';

/** 知识点实体（对应后端 KnowledgePoint PO） */
export interface KnowledgePoint {
  knowledgePointId: string;
  name: string;
  stage: string;
  subject: string;
  difficulty: number; // 1-3
  description?: string;
  cover?: string;
  lessonId?: string;
  sort?: number;
  status: number; // 0=停用 1=启用
  createTime?: string;
  updateTime?: string;
}

/** 知识点查询参数 */
export interface KnowledgePointQuery extends PageParam {
  nameFuzzy?: string;
  stage?: string;
  subject?: string;
  difficulty?: number;
}

/** 分页加载知识点列表 */
export function loadDataList(query: KnowledgePointQuery): Promise<PageResult<KnowledgePoint>> {
  return request.get('/knowledgePoint/loadDataList', { params: query });
}

/** 获取知识点详情 */
export function getInfo(knowledgePointId: string): Promise<KnowledgePoint> {
  return request.get('/knowledgePoint/getInfo', { params: { knowledgePointId } });
}

/** 新增知识点 */
export function add(data: Partial<KnowledgePoint>): Promise<void> {
  return request.post('/knowledgePoint/add', data);
}

/** 修改知识点 */
export function update(data: Partial<KnowledgePoint>): Promise<void> {
  return request.put('/knowledgePoint/update', data);
}

/** 删除知识点 */
export function del(knowledgePointId: string): Promise<void> {
  return request.delete('/knowledgePoint/del', { params: { knowledgePointId } });
}
