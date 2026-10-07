import { get, post } from './request';

/**
 * 学生端新手引导（计划 D1）。
 *
 * 后端只存进度（看没看过、看到第几步、哪一版），步骤文案与高亮目标放前端按学段配置。
 */

/** 引导状态 */
export interface OnboardingStatusVO {
  /** 是否第一次（既没看过欢迎卡也没跳过）→ 自动弹欢迎卡 */
  firstTime: boolean;
  welcomeSeen: boolean;
  skipped: boolean;
  /** 已看完的引导版本 */
  version: number;
  /** 当前脚本版本（前端配置） */
  scriptVersion: number;
  /** 是否有更新（已看版本 < 当前版本） */
  needUpdate: boolean;
  /** 已完成的步骤 key（中途退出可续播） */
  stepsDone: string[];
}

/** 取引导状态 */
export function getOnboardingStatus() {
  return get<OnboardingStatusVO>('/onboarding/status');
}

/** 记录看过欢迎卡 / 选择先自己看看 */
export function recordOnboardingWelcome(data: { welcomeSeen?: boolean; skipped?: boolean }) {
  return post<void>('/onboarding/welcome', data);
}

/** 批量上报步骤进度（步骤完成或退出时上报一次即可） */
export function recordOnboardingSteps(data: { steps: string[]; finished?: boolean }) {
  return post<void>('/onboarding/step', data);
}

/** 重看导览（清空已完成步骤） */
export function resetOnboarding() {
  return post<void>('/onboarding/reset', {});
}

/** 打开引导中心（只记时间） */
export function touchOnboardingOpen() {
  return post<void>('/onboarding/open', {});
}
