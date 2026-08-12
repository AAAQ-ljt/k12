import { get, post, put } from './request';
import type { UserInfo } from '@/types/common';

/** 验证码返回 */
export interface CheckCodeResult {
  checkCodeBase64: string;
  checkCodeKey: string;
}

/** 登录参数 */
export interface LoginParams {
  email: string;
  password: string;
  checkCodeKey: string;
  checkCode: string;
}

/** 注册参数 */
export interface RegisterParams {
  username: string;
  email: string;
  password: string;
  checkCodeKey: string;
  checkCode: string;
}

/** 登录结果 */
export interface LoginResult {
  token: string;
  userInfo: UserInfo;
}

/** 获取图形验证码 */
export function studentCheckCode(): Promise<CheckCodeResult> {
  return get('/studentInfo/checkCode');
}

/** 学生登录 */
export function studentLogin(params: LoginParams): Promise<LoginResult> {
  return post('/studentInfo/login', params);
}

/** 学生注册（注册即登录，返回 token + userInfo） */
export function studentRegister(params: RegisterParams): Promise<LoginResult> {
  return post('/studentInfo/register', params);
}

/** 学生退出登录 */
export function studentLogout(): Promise<void> {
  return post('/studentInfo/logout');
}

/** 获取当前登录学生信息 */
export function getStudentInfo(): Promise<UserInfo> {
  return get('/studentInfo/getUserInfo');
}

/** 修改学段 */
export function updateStudentStage(stage: string): Promise<void> {
  return put('/studentInfo/updateStage', undefined, { params: { stage } });
}
