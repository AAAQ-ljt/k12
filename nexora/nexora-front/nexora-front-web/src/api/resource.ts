import { get } from './request';
import type { PageParam, PageResult } from '@/types/common';

/** 学生端课程教材资源（不包含内部存储路径） */
export interface StudentResourceInfo {
  resourceId: string;
  resourceName: string;
  resourceType: string;
  tags?: string;
  description?: string;
  fileSize?: number;
  cover?: string;
  duration?: number;
  stage?: string;
  knowledgePointId?: string;
  source?: number;
  status: number;
  createTime?: string;
  updateTime?: string;
}

/** 学生端资源查询参数 */
export interface StudentResourceQuery extends PageParam {
  resourceNameFuzzy?: string;
  resourceType?: string;
}

/** 加载当前学段可用教材资源 */
export function loadResourceList(query: StudentResourceQuery): Promise<PageResult<StudentResourceInfo>> {
  return get('/resourceInfo/loadDataList', query);
}

/** 获取资源详情 */
export function getResourceInfo(resourceId: string): Promise<StudentResourceInfo> {
  return get('/resourceInfo/getInfo', { resourceId });
}

/** 视频播放地址（HLS 播放列表） */
export function getResourceVideoUrl(resourceId: string): string {
  return `/api/resourceInfo/video/${resourceId}/index.m3u8`;
}

/** 图片预览地址 */
export function getResourceImageUrl(resourceId: string): string {
  return `/api/resourceInfo/image/${resourceId}`;
}

/** 文档预览地址（原始文件流） */
export function getResourceFileUrl(resourceId: string): string {
  return `/api/resourceInfo/file/${resourceId}`;
}

/** 文件下载地址 */
export function getResourceDownloadUrl(resourceId: string): string {
  return `/api/resourceInfo/download/${resourceId}`;
}
