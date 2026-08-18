import { request } from './request';
import type { PageParam, PageResult } from '@/types/common';

/** 资源实体（对应后端 ResourceInfo PO） */
export interface ResourceInfo {
  resourceId: string;
  resourceName: string;
  resourceType: string; // VIDEO/IMAGE/DOCUMENT（兼容历史 PPT/WORD/PICTURE_BOOK）
  tags?: string;
  description?: string;
  filePath?: string;
  fileSize?: number;
  directoryId?: string;
  ownerId?: string;
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
  resourceNameFuzzy?: string;
  resourceType?: string;
  stage?: string;
  directoryId?: string;
  status?: number;
}

/** 资源新增元数据 */
export interface ResourceAddMetadata {
  resourceName: string;
  resourceType: string;
  stage?: string;
  knowledgePointId?: string;
  directoryId?: string;
}

/** 分片上传会话 */
export interface ResourceUploadSession {
  uploadId: string;
  resourceId: string;
  shardSize: number;
  totalShards: number;
  uploadedShardIndexes: number[];
}

/** 创建分片上传会话参数 */
export interface ResourcePrepareUploadParams {
  resourceName: string;
  resourceType: string;
  fileName: string;
  fileSize: number;
  stage?: string;
  directoryId?: string;
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
export function add(file: File, metadata: ResourceAddMetadata): Promise<string> {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('resourceName', metadata.resourceName);
  formData.append('resourceType', metadata.resourceType);
  if (metadata.stage) formData.append('stage', metadata.stage);
  if (metadata.knowledgePointId) formData.append('knowledgePointId', metadata.knowledgePointId);
  if (metadata.directoryId) formData.append('directoryId', metadata.directoryId);
  return request.post('/resourceInfo/add', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
}

/** 创建分片上传会话 */
export function prepareUpload(params: ResourcePrepareUploadParams): Promise<ResourceUploadSession> {
  return request.post('/resourceInfo/prepareUpload', null, { params });
}

/** 上传单个分片 */
export function uploadShard(uploadId: string, shardIndex: number, file: Blob, fileName: string): Promise<void> {
  const formData = new FormData();
  formData.append('uploadId', uploadId);
  formData.append('shardIndex', String(shardIndex));
  formData.append('file', file, fileName);
  return request.post('/resourceInfo/uploadShard', formData, {
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

/** 批量转移文件目录 */
export function moveResources(resourceIds: string[], directoryId: string): Promise<void> {
  return request.put('/resourceInfo/move', { resourceIds, directoryId });
}

/** 视频播放地址（HLS 播放列表） */
export function getVideoPlaylistUrl(resourceId: string): string {
  return `/api/resourceInfo/video/${resourceId}/index.m3u8`;
}

/** 图片预览地址 */
export function getImagePreviewUrl(resourceId: string): string {
  return `/api/resourceInfo/image/${resourceId}`;
}

/** 文档预览地址（原始文件流） */
export function getFilePreviewUrl(resourceId: string): string {
  return `/api/resourceInfo/file/${resourceId}`;
}

/** 文件下载地址（下载原始文件） */
export function getDownloadUrl(resourceId: string): string {
  return `/api/resourceInfo/download/${resourceId}`;
}

/** 学生个人资源 HLS 播放地址（管理端学习分析预览） */
export function getStudentVideoPlaylistUrl(resourceId: string, userId: string): string {
  return `/api/resourceInfo/studentVideo/${resourceId}/index.m3u8?userId=${encodeURIComponent(userId)}`;
}

/** 学生个人资源图片预览地址 */
export function getStudentImagePreviewUrl(resourceId: string, userId: string): string {
  return `/api/resourceInfo/studentImage/${resourceId}?userId=${encodeURIComponent(userId)}`;
}

/** 学生个人资源文档预览地址 */
export function getStudentFilePreviewUrl(resourceId: string, userId: string): string {
  return `/api/resourceInfo/studentFile/${resourceId}?userId=${encodeURIComponent(userId)}`;
}

/** 学生个人资源下载地址 */
export function getStudentDownloadUrl(resourceId: string, userId: string): string {
  return `/api/resourceInfo/studentDownload/${resourceId}?userId=${encodeURIComponent(userId)}`;
}

/** 批量删除参数 */
export interface ResourceBatchDeleteParams {
  resourceIds: string[];
  dirIds: string[];
}

/** 批量删除文件和空目录 */
export function batchDeleteResources(data: ResourceBatchDeleteParams): Promise<void> {
  return request.delete('/resourceInfo/batchDel', { data });
}
