import { request } from './request';
import type { UserInfo } from '@/types/common';

export interface LoginParams {
  username: string;
  password: string;
}

export interface LoginResult {
  token: string;
  userInfo: UserInfo;
}

/** 管理员登录 */
export function adminLogin(params: LoginParams): Promise<LoginResult> {
  return request.post('/account/login', params);
}

/** 管理员退出登录 */
export function adminLogout(): Promise<void> {
  return request.post('/account/logout');
}

/** 获取当前登录管理员信息（刷新页面后拉取） */
export function getAdminInfo(): Promise<UserInfo> {
  return request.get('/account/getUserInfo');
}
