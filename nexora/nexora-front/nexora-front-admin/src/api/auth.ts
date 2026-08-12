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

/** 获取当前登录管理员信息 - 返回默认值（已在登录时获取）*/
export function getAdminInfo(): Promise<UserInfo> {
  // 登录后前端已有用户信息，这里返回一个默认的 UserInfo
  return Promise.resolve({
    userId: 'admin_001',
    username: 'admin',
    email: 'admin@example.com',
    stage: '',
    roleType: 0, // 0=admin
    avatar: '',
  });
}
