import { request } from './request';
import type { PageParam, PageResult } from '@/types/common';

/** 资源实体（对应后端 ResourceInfo PO） */
export interface ResourceInfo {
  resourceId: string;
  resourceName: string;
  resourceType: string; // VIDEO/DOCUMENT/PPT/WORD/IMAGE/PICTURE_BOOK
  tags?: string;
  description?: string;
  filePath?: string;
  fileSize?: number;
  cover?: string;
  duration?: number;
  hlsPath?: string;
  stage?: string;
  knowledgePointId?: string;
  source?: number; // 0=后台上传 1=AI生成
  status: number; // 0=处理中 1=可用 2=失败
  createBy?: number;
  createTime?: string;
  updateTime?: string;
}

/** 资源查询参数 */
export interface ResourceInfoQuery extends PageParam {
  resourceName?: string;
  resourceType?: string;
  stage?: string;
}

/** 资源新增元数据 */
export interface ResourceAddMetadata {
  resourceName: string;
  resourceType: string;
  stage?: string;
  knowledgePointId?: string;
}

/** 分页加载资源列表 */
export function loadDataList(query: ResourceInfoQuery): Promise<PageResult<ResourceInfo>> {
  return request.get('/resourceInfo/loadDataList', { params: query });
}

/** 获取资源详情 */
export function getInfo(resourceId: string): Promise<ResourceInfo> {
  return request.get('/resourceInfo/getInfo', { params: { resourceId } });
}

/** 新增资源（multipart 上传文件 + 元数据） */
export function add(file: File, metadata: ResourceAddMetadata): Promise<void> {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('resourceName', metadata.resourceName);
  formData.append('resourceType', metadata.resourceType);
  if (metadata.stage) formData.append('stage', metadata.stage);
  if (metadata.knowledgePointId) formData.append('knowledgePointId', metadata.knowledgePointId);
  return request.post('/resourceInfo/add', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
}

/** 修改资源元信息 */
export function update(data: Partial<ResourceInfo>): Promise<void> {
  return request.put('/resourceInfo/update', data);
}

/** 删除资源 */
export function del(resourceId: string): Promise<void> {
  return request.delete('/resourceInfo/del', { params: { resourceId } });
}
