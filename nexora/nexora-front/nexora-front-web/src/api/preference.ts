import { get, post } from './request';

/**
 * 学生端「AI 偏好设置」（计划 C2）：自定义提示词规则的读写。
 *
 * 规则只调整回答的称呼/长短/风格/举例方式，优先级低于平台安全与事实规则；
 * 越权内容（如"忽略以上规则"）会被服务端拒绝并返回可操作的改写建议。
 */

/** 规则（与我设置的一条偏好） */
export interface UserPromptRuleItem {
  ruleId: number;
  ruleType: string;
  ruleValue: string;
  scope?: string;
  /** 1启用 0停用 */
  status: number;
  /** STUDENT 学生自填 / AI_SUGGEST AI提议后确认 / SYSTEM 系统默认 */
  source?: string;
  createTime?: string;
}

/** 规则类型选项 */
export interface PromptRuleTypeItem {
  code: string;
  /** 展示名，如「怎么称呼我」 */
  name: string;
}

/** 偏好设置页数据 */
export interface UserPromptRuleListVO {
  enabled: boolean;
  maxCount: number;
  enabledCount: number;
  rules: UserPromptRuleItem[];
  types: PromptRuleTypeItem[];
}

/** 我的偏好列表（规则 + 类型 + 条数上限） */
export function loadMyPromptRules() {
  return get<UserPromptRuleListVO>('/userPromptRule/loadDataList');
}

/** 新增一条偏好，返回规则 ID */
export function addPromptRule(data: { ruleType: string; ruleValue: string }) {
  return post<number>('/userPromptRule/add', data);
}

/** 停用/启用一条偏好 */
export function changePromptRuleStatus(ruleId: number, status: number) {
  return post<void>('/userPromptRule/changeStatus', { ruleId, status });
}

/** 删除一条偏好 */
export function deletePromptRule(ruleId: number) {
  return post<void>('/userPromptRule/del', { ruleId });
}

/** 《我的学习偏好》系统页（计划 C3） */
export interface PreferencePageVO {
  docId: string;
  title: string;
  content: string;
  vectorStatus?: number;
}

/** 打开偏好页（首次访问自动创建） */
export function getPreferencePage() {
  return get<PreferencePageVO>('/preferencePage/getInfo');
}

/** 保存正文（规则段由系统重新渲染，你写的内容保留） */
export function savePreferencePage(content: string) {
  return post<PreferencePageVO>('/preferencePage/save', { content });
}

/** 重置：清空自填规则 + 正文回初始内容 */
export function resetPreferencePage() {
  return post<PreferencePageVO>('/preferencePage/reset', {});
}
