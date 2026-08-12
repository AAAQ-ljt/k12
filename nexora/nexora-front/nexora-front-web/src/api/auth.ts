import { get, post } from './request';
import type { UserInfo } from '@/types/common';

/** 登录参数 */
export interface LoginParams {
  email: string;
  password: string;
}

/** 登录结果 */
export interface LoginResult {
  token: string;
  userInfo: UserInfo;
}

/** 学生登录 */
export function studentLogin(params: LoginParams): Promise<LoginResult> {
  return post('/studentInfo/login', params);
}

/** 学生退出登录 */
export function studentLogout(): Promise<void> {
  return post('/studentInfo/logout');
}

/** 获取当前登录学生信息 */
export function getStudentInfo(): Promise<UserInfo> {
  return get('/studentInfo/getUserInfo');
}
