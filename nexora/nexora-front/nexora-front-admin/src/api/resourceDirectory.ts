import { request } from './request';

/** 资源目录实体 */
export interface ResourceDirectory {
  dirId: string;
  dirName: string;
  parentId: string;
  sort: number;
  createTime?: string;
  updateTime?: string;
}

/** 获取目录树（扁平列表，前端组树） */
export function getTree(): Promise<ResourceDirectory[]> {
  return request.get('/resourceDirectory/getTree');
}

/** 新建目录 */
export function addDirectory(data: { dirName: string; parentId: string }): Promise<void> {
  return request.post('/resourceDirectory/add', data);
}

/** 重命名目录 */
export function updateDirectory(data: { dirId: string; dirName: string }): Promise<void> {
  return request.put('/resourceDirectory/update', data);
}

/** 删除目录 */
export function delDirectory(dirId: string): Promise<void> {
  return request.delete('/resourceDirectory/del', { params: { dirId } });
}

/** 同级目录排序 */
export function sortDirectory(parentId: string, dirIds: string[]): Promise<void> {
  return request.put('/resourceDirectory/sort', { parentId, dirIds });
}
