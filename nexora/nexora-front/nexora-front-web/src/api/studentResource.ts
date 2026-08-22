import { del, get, post, put } from './request';
import type { PageParam, PageResult } from '@/types/common';
import type { StudentResourceInfo } from './resource';

export interface StudentDirectory {
  dirId: string;
  dirName: string;
  parentId: string;
  /** 系统目录类型：raw/wiki/attachments；普通目录为空 */
  dirType?: string;
  ownerId?: string;
  sort: number;
  createTime?: string;
  updateTime?: string;
}

export interface StudentResource extends StudentResourceInfo {
  directoryId?: string;
}

export interface StudentResourceQuery extends PageParam {
  directoryId?: string;
  resourceNameFuzzy?: string;
  resourceType?: string;
}

export interface StudentUploadSession {
  uploadId: string;
  resourceId: string;
  shardSize: number;
  totalShards: number;
  uploadedShardIndexes: number[];
}

export interface StudentStorageInfo {
  usedBytes: number;
  quotaBytes: number;
  remainingBytes: number;
  initialized: boolean;
}

export function loadStudentStorage(): Promise<StudentStorageInfo> {
  return get('/studentResource/storage');
}

export function initStudentKnowledgeBase(): Promise<void> {
  return post('/studentResource/initKnowledgeBase');
}

export function loadStudentDirectories(): Promise<StudentDirectory[]> {
  return get('/studentResource/directoryTree');
}

export function addStudentDirectory(data: { dirName: string; parentId: string }): Promise<string> {
  return post('/studentResource/directory', data);
}

export function updateStudentDirectory(data: { dirId: string; dirName: string }): Promise<void> {
  return put('/studentResource/directory', data);
}

export function deleteStudentDirectory(dirId: string): Promise<void> {
  return del('/studentResource/directory', { dirId });
}

export function sortStudentDirectories(dirIds: string[]): Promise<void> {
  return put('/studentResource/directory/sort', { dirIds });
}

export function loadStudentResources(query: StudentResourceQuery): Promise<PageResult<StudentResource>> {
  return get('/studentResource/list', query);
}

export function getStudentResource(resourceId: string): Promise<StudentResource> {
  return get('/studentResource/getInfo', { resourceId });
}

export function updateStudentResource(data: {
  resourceId: string;
  resourceName?: string;
  description?: string;
  directoryId?: string;
}): Promise<void> {
  return put('/studentResource/update', data);
}

export function deleteStudentResource(resourceId: string): Promise<void> {
  return del('/studentResource/del', { resourceId });
}

export function prepareStudentUpload(data: {
  resourceName: string;
  resourceType: string;
  fileName: string;
  fileSize: number;
  directoryId?: string;
}): Promise<StudentUploadSession> {
  const form = new FormData();
  form.append('resourceName', data.resourceName);
  form.append('resourceType', data.resourceType);
  form.append('fileName', data.fileName);
  form.append('fileSize', String(data.fileSize));
  if (data.directoryId) {
    form.append('directoryId', data.directoryId);
  }
  return post('/studentResource/prepareUpload', form);
}

export function uploadStudentShard(uploadId: string, shardIndex: number, blob: Blob): Promise<void> {
  const form = new FormData();
  form.append('uploadId', uploadId);
  form.append('shardIndex', String(shardIndex));
  form.append('file', blob);
  return post('/studentResource/uploadShard', form, { timeout: 120000 });
}

export function getStudentResourceVideoUrl(resourceId: string): string {
  return `/api/resourceInfo/video/${resourceId}/index.m3u8`;
}

export function getStudentResourceImageUrl(resourceId: string): string {
  return `/api/resourceInfo/image/${resourceId}`;
}

export function getStudentResourceFileUrl(resourceId: string): string {
  return `/api/resourceInfo/file/${resourceId}`;
}

export function getStudentResourceDownloadUrl(resourceId: string): string {
  return `/api/resourceInfo/download/${resourceId}`;
}
