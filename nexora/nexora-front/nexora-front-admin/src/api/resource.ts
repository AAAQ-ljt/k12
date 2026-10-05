import axios from 'axios';
import { request } from './request';
import { getToken } from '@/utils/token';
import type { PageParam, PageResult } from '@/types/common';

/** 单个分片请求的超时（毫秒）：5MB 分片在慢速上行下会远超全局 15 秒，这里单独放宽 */
const SHARD_UPLOAD_TIMEOUT_MS = 120000;

/** 分片上传最大尝试次数（含首次）：网络抖动 / 超时自动重试，业务错误不重试 */
const SHARD_UPLOAD_MAX_ATTEMPTS = 3;

function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => {
    window.setTimeout(resolve, ms);
  });
}

/** 是否值得重试：仅网络异常、超时、5xx / 408 / 429；业务码错误（会话过期、分片序号非法）重试无意义 */
function isRetryableShardError(error: unknown): boolean {
  if (!axios.isAxiosError(error)) {
    return false;
  }
  const status = error.response?.status;
  if (status === undefined) {
    return true;
  }
  return status >= 500 || status === 408 || status === 429;
}

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
  stageIncludeNull?: boolean;
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

/** 上传单个分片（自动重试：5MB 分片在弱网下容易超时，单次失败不再直接废掉整个文件） */
export async function uploadShard(uploadId: string, shardIndex: number, file: Blob, fileName: string): Promise<void> {
  const formData = new FormData();
  formData.append('uploadId', uploadId);
  formData.append('shardIndex', String(shardIndex));
  formData.append('file', file, fileName);
  let lastError: unknown;
  for (let attempt = 1; attempt <= SHARD_UPLOAD_MAX_ATTEMPTS; attempt += 1) {
    try {
      await request.post('/resourceInfo/uploadShard', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        timeout: SHARD_UPLOAD_TIMEOUT_MS,
      });
      return;
    } catch (error) {
      lastError = error;
      if (attempt >= SHARD_UPLOAD_MAX_ATTEMPTS || !isRetryableShardError(error)) {
        break;
      }
      await sleep(attempt * 1000);
    }
  }
  throw lastError instanceof Error ? lastError : new Error('分片上传失败');
}

/** 放弃上传上报参数 */
export interface ResourceUploadAbandonParams {
  uploadId?: string;
  resourceId: string;
}

/**
 * 上报「放弃上传」（页面关闭 / 刷新、分片重试耗尽）：让后端立即把资源收敛为「失败」，
 * 不必再等僵尸清扫窗口。
 *
 * keepalive=true 时走 fetch + keepalive：页面卸载后请求仍能送达，且可携带 adminToken 头
 * （sendBeacon 无法设置请求头，会被登录拦截器判 401，故不用）
 */
export function abandonUpload(params: ResourceUploadAbandonParams, keepalive = false): Promise<void> {
  if (!keepalive) {
    return request.post('/resourceInfo/abandonUpload', params);
  }
  const token = getToken();
  return fetch('/api/resourceInfo/abandonUpload', {
    method: 'POST',
    keepalive: true,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { adminToken: token } : {}),
    },
    body: JSON.stringify(params),
  }).then(() => undefined);
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

/** 在线预览产物元信息（Office 文档转 PDF/逐页图的状态与页数；未生成过为 null） */
export interface ResourcePreviewMeta {
  status?: 'GENERATING' | 'READY' | 'FAILED';
  pages?: number;
  attempts?: number;
  sourceSize?: number;
  generatedAt?: string;
  message?: string;
}

/** 读取在线预览产物元信息 */
export function getPreviewMeta(resourceId: string): Promise<ResourcePreviewMeta | null> {
  return request.get(`/resourceInfo/preview/${resourceId}/meta`);
}

/** 在线预览单页图片地址（按页懒加载，首屏只拉第 1 页） */
export function getPreviewPageUrl(resourceId: string, page: number): string {
  return `/api/resourceInfo/preview/${resourceId}/page/${page}`;
}

/** 在线预览 PDF 地址（服务端转换产物，供新窗口打开/下载） */
export function getPreviewPdfUrl(resourceId: string): string {
  return `/api/resourceInfo/preview/${resourceId}/pdf`;
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
