import { request } from './request';
import type { PageParam, PageResult } from '@/types/common';

/** 用户实体（对应后端 UserInfo PO） */
export interface UserInfo {
  userId: number;
  username: string;
  email: string;
  password?: string;
  nickName?: string;
  avatar?: string;
  roleType: number; // 0=管理员 1=学生
  stage?: string;
  grade?: string;
  sex?: number; // 0=女 1=男 2=保密
  status: number; // 0=禁用 1=启用
  lastLoginTime?: string;
  createTime?: string;
  updateTime?: string;
}

/** 用户查询参数 */
export interface UserQuery extends PageParam {
  /** 邮箱模糊查询（后端 LIKE） */
  emailFuzzy?: string;
  stage?: string;
  status?: number;
  roleType?: number;
}

/** 分页加载用户列表 */
export function loadDataList(query: UserQuery): Promise<PageResult<UserInfo>> {
  return request.get('/userInfo/loadDataList', { params: query });
}

/** 获取用户详情 */
export function getInfo(userId: number): Promise<UserInfo> {
  return request.get('/userInfo/getInfo', { params: { userId } });
}

/** 新增用户 */
export function add(data: Partial<UserInfo>): Promise<void> {
  return request.post('/userInfo/add', data);
}

/** 修改用户（password 为空则不修改） */
export function update(data: Partial<UserInfo>): Promise<void> {
  return request.put('/userInfo/update', data);
}

/** 删除用户 */
export function del(userId: number): Promise<void> {
  return request.delete('/userInfo/del', { params: { userId } });
}

/** 修改用户状态 */
export function changeStatus(userId: number, status: number): Promise<void> {
  return request.put('/userInfo/changeStatus', undefined, { params: { userId, status } });
}
