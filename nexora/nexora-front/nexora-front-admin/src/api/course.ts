import { request } from './request';
import type { PageParam, PageResult } from '@/types/common';

/** 课程实体（对应后端 CourseInfo PO） */
export interface CourseInfo {
  courseId: string;
  courseName: string;
  cover?: string;
  stage: string;
  subject: string;
  difficulty?: number; // 1-3
  description?: string;
  intro?: string;
  lessonCount?: number;
  studyCount?: number;
  sort?: number;
  status: number; // 0=下架 1=上架
  createBy?: number;
  createTime?: string;
  updateTime?: string;
}

/** 课程查询参数 */
export interface CourseInfoQuery extends PageParam {
  courseName?: string;
  stage?: string;
  status?: number;
}

/** 分页加载课程列表 */
export function loadDataList(query: CourseInfoQuery): Promise<PageResult<CourseInfo>> {
  return request.get('/courseInfo/loadDataList', { params: query });
}

/** 获取课程详情 */
export function getInfo(courseId: string): Promise<CourseInfo> {
  return request.get('/courseInfo/getInfo', { params: { courseId } });
}

/** 新增课程 */
export function add(data: Partial<CourseInfo>): Promise<void> {
  return request.post('/courseInfo/add', data);
}

/** 修改课程 */
export function update(data: Partial<CourseInfo>): Promise<void> {
  return request.put('/courseInfo/update', data);
}

/** 删除课程 */
export function del(courseId: string): Promise<void> {
  return request.delete('/courseInfo/del', { params: { courseId } });
}
